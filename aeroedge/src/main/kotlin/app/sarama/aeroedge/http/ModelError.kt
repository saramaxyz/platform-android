package app.sarama.aeroedge.http

sealed class ModelError(override val message: String): Throwable() {

    data object InvalidUrl: ModelError("The URL provided is invalid.")

    data object FileNotFound: ModelError("The file could not be found on the device.")

    class DownloadError(errorMessage: String): ModelError("An error occurred during the download: $errorMessage")

    class CompilationError(errorMessage: String): ModelError("An error occurred during the compilation: $errorMessage")

    data object NetworkError: ModelError("An error occurred during the network request.")

    data object FileWriteError: ModelError("An error occurred during the file writing.")

    class FailedToLoadModel(errorMessage: String): ModelError("An error occurred while loading the model: $errorMessage")

    data object ModelNotFound: ModelError("The model cannot be found.")
}