package app.sarama.aeroedge.service.autocomplete

sealed class InitializationStatus {

    data object NotInitialized: InitializationStatus()
    data object Initializing: InitializationStatus()
    data object Initialized: InitializationStatus()
    data class Error(val exception: Throwable): InitializationStatus()
}
