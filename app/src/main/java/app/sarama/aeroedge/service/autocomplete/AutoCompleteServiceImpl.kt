package app.sarama.aeroedge.service.autocomplete

import androidx.annotation.WorkerThread
import app.sarama.aeroedge.AeroEdge
import app.sarama.aeroedge.ModelStatusUpdate
import app.sarama.aeroedge.ModelType
import app.sarama.aeroedge.util.splitToWords
import app.sarama.aeroedge.util.trimToMaxWordCount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min


class AutoCompleteServiceImpl(
    private val aeroEdge: AeroEdge,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AutoCompleteService {

    private val modelStatusFlow = MutableStateFlow<ModelStatusUpdate?>(null)
    private var interpreter: Interpreter? = null
    private val outputBuffer = ByteBuffer.allocateDirect(OutputBufferSize)
    override val initializationStatus: InitializationStatus
        get() = when (val status = modelStatusFlow.value) {
            null -> InitializationStatus.NotInitialized
            is ModelStatusUpdate.OnCompleted -> InitializationStatus.Initialized
            is ModelStatusUpdate.InProgress -> InitializationStatus.Initializing
            is ModelStatusUpdate.OnFailed -> InitializationStatus.Error(status.exception)
        }

    override val inputConfiguration = AutoCompleteInputConfiguration(
        // Minimum number of words to be taken from the end of the input text
        minWordCount = 5,
        // Maximum number of words to be taken from the end of the input text, limited by what the model allows
        maxWordCount = min(50, MaxInputWordCount),
        // Initially selected value for number of words to be taken from the end of the input text
        initialWordCount = 20
    )

    override suspend fun loadModel(): Result<Unit> {
        if (initializationStatus == InitializationStatus.Initializing) {
            return Result.failure(AutoCompleteServiceError.ModelAlreadyLoading)
        }

        withContext(dispatcher) {
            launch {
                aeroEdge.getModel(ModelName, ModelType.TensorFlowLite).collect {
                    modelStatusFlow.value = it

                    when (it) {
                        is ModelStatusUpdate.OnCompleted -> {
                            interpreter = Interpreter(it.model.localFile.fileChannel)
                            println("[AEROEDGE] Loaded!")
                        }

                        is ModelStatusUpdate.InProgress -> println("[AEROEDGE] Loading ${(it.progress * 100).toInt()}%")
                        is ModelStatusUpdate.OnFailed -> {
                            println("[AEROEDGE] On error! ")
                            it.exception.printStackTrace()
                        }

                        else -> Unit
                    }
                }
            }

        }

        return Result.success(Unit)
    }

    private val File.fileChannel: MappedByteBuffer
        get() = FileInputStream(this).channel.map(FileChannel.MapMode.READ_ONLY, 0, length())

    override suspend fun autocomplete(
        input: String,
        applyWindow: Boolean,
        windowSize: Int,
    ): Result<List<String>> = withContext(dispatcher) {
        val maxInputWordCount = if (applyWindow) windowSize else MaxInputWordCount
        val trimmedInput = input.trimToMaxWordCount(maxInputWordCount)

        val output = runInterpreterOn(trimmedInput)

        if (output.length < trimmedInput.length) {
            return@withContext Result.failure(AutoCompleteServiceError.NoSuggestion)
        }

        val newText = output.substring(output.indexOf(trimmedInput) + trimmedInput.length)
        val words = newText.splitToWords()
        if (words.isEmpty()) {
            return@withContext Result.failure(AutoCompleteServiceError.NoSuggestion)
        }

        Result.success(words)
    }

    @WorkerThread
    private fun runInterpreterOn(input: String): String {
        outputBuffer.clear()

        // Run interpreter, which will generate text into outputBuffer
        interpreter?.run(input, outputBuffer)

        // Set output buffer limit to current position & position to 0
        outputBuffer.flip()

        // Get bytes from output buffer
        val bytes = ByteArray(outputBuffer.remaining())
        outputBuffer.get(bytes)

        outputBuffer.clear()

        // Return bytes converted to String
        return String(bytes, Charsets.UTF_8)
    }

    private companion object {
        const val ModelName = "autocomplete"
        const val MaxInputWordCount = 1024
        const val OutputBufferSize = 800
    }
}