package app.sarama.aeroedge

@JvmInline
value class ModelType(val extensionName: String) {

    companion object {
        val TensorFlowLite = ModelType("tflite")
    }
}
