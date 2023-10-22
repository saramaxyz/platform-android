package app.sarama.aeroedge.service.autocomplete

data class AutoCompleteInputConfiguration(
    // Minimum number of words to be taken from the end of the input text
    val minWordCount: Int = 5,
    // Maximum number of words to be taken from the end of the input text
    val maxWordCount: Int = 50,
    // Initially selected value for number of words to be taken from the end of the input text
    val initialWordCount: Int = 20,
)