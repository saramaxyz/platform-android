package app.sarama.aeroedge.service.autocomplete

sealed class AutoCompleteServiceError(message: String): Throwable(message) {

    data object ModelAlreadyLoading: AutoCompleteServiceError("Model already loading, no need to load it again.")
    data object NoSuggestion: AutoCompleteServiceError("No autocomplete suggestion found.")
}