package app.sarama.aeroedge

sealed class ModelStatusUpdate {

    data class InProgress(val progress: Float): ModelStatusUpdate()

    data class OnCompleted(val model: Model, val isModelUpToDate: Boolean): ModelStatusUpdate()

    data class OnFailed(val exception: Throwable): ModelStatusUpdate()

}