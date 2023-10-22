package app.sarama.aeroedge.ui.screen.autocomplete

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sarama.aeroedge.service.autocomplete.AutoCompleteService
import app.sarama.aeroedge.service.autocomplete.AutoCompleteServiceError
import app.sarama.aeroedge.service.autocomplete.InitializationStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AutoCompleteViewModel(
    private val autoCompleteService: AutoCompleteService
) : ViewModel() {

    private var currentSuggestionInputText: String = ""
    private var currentSuggestionText: String? = null
    private var windowSize = 0
    private var initModelError: Throwable? = null
    private var previousSuggestionsIndex = 0
    private val isGenerating = MutableStateFlow(false)
    private val hasGenerated = MutableStateFlow(false)
    private val isSuggesting = MutableStateFlow(false)
    private val modelInitializationStatus = MutableStateFlow<InitializationStatus>(InitializationStatus.NotInitialized)

    val modelStatusUpdate: StateFlow<InitializationStatus>
        get() = modelInitializationStatus

    init {
        modelInitializationStatus.value = autoCompleteService.initializationStatus

        if(modelInitializationStatus.value == InitializationStatus.NotInitialized) {
            viewModelScope.launch {
                autoCompleteService.loadModel(viewModelScope).collect {
                    modelInitializationStatus.emit(it)
                }
            }
        }
    }

    private val _isTextEmpty = MutableStateFlow(true)
    var isTextEmpty: Boolean = true
        set(value) {
            field = value
            _isTextEmpty.value = value

            // Clear previous suggestions if the input text is empty
            if (value) {
                _previousSuggestions.clear()
            }
        }

    val windowSizeConfiguration = autoCompleteService.inputConfiguration

    /**
     * State flow to reset text to previous value.
     * Needs to be acknowledged by calling [onResetReceived] since the previous value can be the same
     */
    private val _resetInputText = MutableStateFlow<String?>(null)
    val resetInputText: StateFlow<String?>
        get() = _resetInputText

    fun onResetReceived() {
        _resetInputText.value = null
    }

    /**
     * State flow containing most recent suggestion from model, as list of words
     * Needs to be acknowledged by calling [onSuggestionReceived]
     */
    private val _suggestion = MutableStateFlow<List<String>?>(null)
    val suggestion: StateFlow<List<String>?>
        get() = _suggestion

    /**
     * Shared flow exposing errors from autocomplete service
     */
    private val _error = MutableSharedFlow<Throwable>()
    val error: SharedFlow<Throwable>
        get() = _error

    /**
     * State flow exposing previously made suggestions
     */
    private val _previousSuggestions = mutableStateListOf<Suggestion>()
    val previousSuggestions: List<Suggestion>
        get() = _previousSuggestions

    /**
     * State flow exposing whether Clear CTA should be enabled
     */
    private val clearEnabled = combine(isGenerating, _isTextEmpty) { isGenerating, isEmpty ->
        !isGenerating && !isEmpty
    }

    /**
     * State flow exposing whether Generate CTA should be enabled
     */
    private val generateEnabled = combine(isGenerating, _isTextEmpty) { isGenerating, isEmpty ->
        !isGenerating && !isEmpty
    }

    /**
     * State flow exposing whether Copy CTA should be enabled
     */
    private val copyEnabled = combine(isGenerating, hasGenerated) { isGenerating, hasGenerated ->
        !isGenerating && hasGenerated
    }

    /**
     * State flow exposing edit bar state for Clear, Generate & Copy CTAs & generation process state
     */
    private val editingBarState = combine(clearEnabled, generateEnabled, copyEnabled, isGenerating) { clear, generate, copy, generating ->
        TextEditBarState.Editing(
            clearEnabled = clear,
            generateEnabled = generate,
            copyEnabled = copy,
            generating = generating,
        )
    }

    /**
     * State flow exposing edit bar state & whether a suggestion is active
     */
    val textBarState = combine(editingBarState, isSuggesting) { editState, suggesting ->
        if (suggesting) TextEditBarState.Suggesting
        else editState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = initialControlBarState
    )

    /**
     * State flow exposing whether input by user should be possible
     */
    val inputFieldEnabled = combine(modelInitializationStatus, isGenerating, isSuggesting) { modelInitializationStatus, generating, suggesting ->
        modelInitializationStatus is InitializationStatus.Initialized && !generating && !suggesting
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = true
    )

    fun onClearInput() {
        isGenerating.value = false
        hasGenerated.value = false
        _isTextEmpty.value = true
        currentSuggestionInputText = ""

        _previousSuggestions.clear()
    }

    fun onWindowSizeChange(size: Int) {
        windowSize = size
    }

    fun onRetryGenerateAutoComplete() {
        _resetInputText.value = currentSuggestionInputText

        isSuggesting.value = false
        currentSuggestionText = null

        onGenerateAutoComplete(currentSuggestionInputText)
    }

    fun onAcceptSuggestion() {
        currentSuggestionText?.let { text ->
            _previousSuggestions += Suggestion(
                text = text,
                id = previousSuggestionsIndex++
            )
        }

        isSuggesting.value = false
        currentSuggestionText = null
    }

    fun removeMissingSuggestions(ids: List<Int>) {
        for (id in ids) {
            _previousSuggestions.removeIf { suggestion -> suggestion.id == id }
        }
    }

    fun onGenerateAutoComplete(text: String) {
        initModelError?.let { error ->
            viewModelScope.launch {
                _error.emit(error)
            }
            return
        }

        currentSuggestionInputText = text

        isGenerating.value = true

        viewModelScope.launch {
            autoCompleteService.autocomplete(text, applyWindow = true, windowSize = windowSize).fold(
                onSuccess = {words ->
                    _suggestion.value = words
                    currentSuggestionText = words.joinToString(separator = "")
                },
                onFailure = {
                    _error.emit(it)

                    isGenerating.value = false
                }
            )
        }
    }

    fun onSuggestionReceived() {
        _suggestion.value = null

        isGenerating.value = false
        hasGenerated.value = true
        isSuggesting.value = true
    }
}

sealed class TextEditBarState {
    data class Editing(
        val clearEnabled: Boolean,
        val generateEnabled: Boolean,
        val copyEnabled: Boolean,
        val generating: Boolean,
    ) : TextEditBarState()

    data object Suggesting : TextEditBarState()
}

val initialControlBarState: TextEditBarState = TextEditBarState.Editing(
    clearEnabled = false,
    generateEnabled = false,
    copyEnabled = false,
    generating = false
)

data class Suggestion(
    val text: String,
    val id: Int
)