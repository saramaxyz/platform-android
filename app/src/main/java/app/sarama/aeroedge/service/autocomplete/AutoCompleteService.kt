package app.sarama.aeroedge.service.autocomplete

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface AutoCompleteService {

    val initializationStatus: InitializationStatus

    val inputConfiguration: AutoCompleteInputConfiguration

    suspend fun loadModel(scope: CoroutineScope): StateFlow<InitializationStatus>

    suspend fun autocomplete(input: String, applyWindow: Boolean = false, windowSize: Int = 50): Result<List<String>>
}