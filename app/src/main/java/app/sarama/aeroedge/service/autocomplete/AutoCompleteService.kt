package app.sarama.aeroedge.service.autocomplete

interface AutoCompleteService {

    val initializationStatus: InitializationStatus

    val inputConfiguration: AutoCompleteInputConfiguration

    suspend fun loadModel(): Result<Unit>

    suspend fun autocomplete(input: String, applyWindow: Boolean = false, windowSize: Int = 50): Result<List<String>>
}