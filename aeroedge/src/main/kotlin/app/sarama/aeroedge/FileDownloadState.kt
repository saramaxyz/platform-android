package app.sarama.aeroedge

internal sealed class FileDownloadState {

    data class OnProgress(val progress: Float): FileDownloadState()

    data class OnFailed(val exception: Throwable): FileDownloadState()

    data object OnSuccess: FileDownloadState()
}
