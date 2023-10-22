package app.sarama.aeroedge.ui.screen.autocomplete

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import app.sarama.aeroedge.R
import app.sarama.aeroedge.service.autocomplete.AutoCompleteInputConfiguration
import app.sarama.aeroedge.ui.screen.autocomplete.components.AutoCompleteInfo
import app.sarama.aeroedge.ui.screen.autocomplete.components.AutoCompleteTextField
import app.sarama.aeroedge.ui.screen.autocomplete.components.TextControlBar
import app.sarama.aeroedge.ui.screen.autocomplete.components.WindowSizeSelection
import app.sarama.aeroedge.ui.theme.AeroEdgeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutoCompleteScreen(
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewmodel = getViewModel<AutoCompleteViewModel>()

    val textValue = rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(annotatedString = AnnotatedString(""))) }
    val barState by viewmodel.textBarState.collectAsState()
    val inputFieldEnabled by viewmodel.inputFieldEnabled.collectAsStateWithLifecycle()
    val windowSizeConfiguration by remember { mutableStateOf(viewmodel.windowSizeConfiguration) }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun showToast(text: String) {
        keyboardController?.hide()

        onShowToast(text)
    }

    val copiedTextStr = stringResource(R.string.text_copied)

    AutoCompleteScreenContent(
        inputValue = textValue.value,
        inputEnabled = inputFieldEnabled,
        onInputValueChange = { value ->
            textValue.value = value
            viewmodel.isTextEmpty = value.text.isEmpty()
        },
        barState = barState,
        inputConfiguration = windowSizeConfiguration,
        onClear = {
            textValue.value = TextFieldValue(AnnotatedString(""))
            viewmodel.onClearInput()
        },
        onCopy = {
            clipboardManager.setText(textValue.value.annotatedString)

            onShowToast(copiedTextStr)
        },
        onGenerate = { viewmodel.onGenerateAutoComplete(textValue.value.text) },
        onRetry = viewmodel::onRetryGenerateAutoComplete,
        onAccept = {
            viewmodel.onAcceptSuggestion()
            textValue.value = TextFieldValue(
                text = textValue.value.text,
                selection = TextRange(textValue.value.text.length)
            )
        },
        onWindowSizeChange = viewmodel::onWindowSizeChange,
        modifier = modifier
    )

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(key1 = Unit) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            launch {
                viewmodel.suggestion.collectLatest { words ->
                    words?.let {
                        animateSuggestion(textValue, words, colorScheme) {
                            viewmodel.onSuggestionReceived()
                        }
                    }
                }
            }
            launch {
                viewmodel.resetInputText.collectLatest { resetText ->
                    resetText?.let { text ->
                        textValue.value = TextFieldValue(
                            annotatedString = AnnotatedString(text),
                            selection = TextRange(text.length)
                        )

                        viewmodel.onResetReceived()
                    }
                }
            }
            launch {
                viewmodel.error.collectLatest { error ->
                    showToast(error.message ?: "Unknown error")
                }
            }
        }
    }
}

suspend fun animateSuggestion(
    textValueState: MutableState<TextFieldValue>,
    words: List<String>,
    colorScheme: ColorScheme,
    onAnimationComplete: () -> Unit
) {
    val builder = AnnotatedString.Builder(textValueState.value.annotatedString)

    val stylePos = builder.pushStyle(
        SpanStyle(
            color = colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    )

    for (word in words) {
        builder.append(word)

        val annotatedString = builder.toAnnotatedString()
        textValueState.value = TextFieldValue(
            annotatedString = annotatedString,
            selection = TextRange(annotatedString.length)
        )
        delay(100)
    }

    builder.pop(stylePos)

    onAnimationComplete()
}

@Composable
fun AutoCompleteScreenContent(
    inputValue: TextFieldValue,
    inputEnabled: Boolean,
    onInputValueChange: (TextFieldValue) -> Unit,
    barState: TextEditBarState,
    inputConfiguration: AutoCompleteInputConfiguration,
    onClear: () -> Unit,
    onCopy: () -> Unit,
    onGenerate: () -> Unit,
    onRetry: () -> Unit,
    onAccept: () -> Unit,
    onWindowSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Column(
                modifier = modifier
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp)
            ) {
                AutoCompleteTextField(
                    inputValue = inputValue,
                    inputEnabled = inputEnabled,
                    onInputValueChange = onInputValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 16.dp),
                )
                TextControlBar(
                    state = barState,
                    onClearClick = onClear,
                    onGenerateClick = onGenerate,
                    onCopyClick = onCopy,
                    onAccept = onAccept,
                    onRetry = onRetry
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            WindowSizeSelection(
                inputConfiguration = inputConfiguration,
                onWindowValueChange = onWindowSizeChange,
                modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            )
            AutoCompleteInfo(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun PreviewAutoCompleteScreen() {
    AeroEdgeTheme {
        val inputValue by remember { mutableStateOf(TextFieldValue()) }
        AutoCompleteScreenContent(
            inputValue = inputValue,
            onInputValueChange = {},
            inputEnabled = true,
            inputConfiguration = AutoCompleteInputConfiguration(),
            onClear = {},
            onCopy = {},
            onGenerate = {},
            onRetry = {},
            onAccept = {},
            onWindowSizeChange = {},
            barState = initialControlBarState,
        )
    }
}