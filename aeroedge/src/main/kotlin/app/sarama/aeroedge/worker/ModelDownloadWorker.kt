package app.sarama.aeroedge.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.sarama.aeroedge.DefaultModelLocalStore
import app.sarama.aeroedge.FileDownloadState
import app.sarama.aeroedge.ModelEntity
import app.sarama.aeroedge.ModelLocalStore
import app.sarama.aeroedge.http.DefaultModelDownloaderClient
import app.sarama.aeroedge.http.ModelDownloaderClient
import java.lang.IllegalArgumentException

internal class ModelDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val modelDownloaderClient: ModelDownloaderClient = DefaultModelDownloaderClient()
    private val localStore: ModelLocalStore = DefaultModelLocalStore(appContext)

    override suspend fun doWork(): Result {
        val modelEntity = inputData.asModelEntity.getOrElse {
            return Result.failure()
        }

        setProgress(workDataOf(WORKER_PROGRESS_KEY to 0.0F))

        var fileDownloadError: Throwable? = null
        val destinationFile = localStore.createLocalModelFile(
            modelEntity.name,
            modelEntity.version,
            modelEntity.fileExtension
        )

        modelDownloaderClient.downloadFile(modelEntity.url, destinationFile).collect {
            when (it) {
                is FileDownloadState.OnProgress -> setProgress(workDataOf(WORKER_PROGRESS_KEY to it.progress))

                is FileDownloadState.OnFailed -> fileDownloadError = it.exception.also { it.printStackTrace() }

                is FileDownloadState.OnSuccess -> Unit
            }
        }

        fileDownloadError?.let {
            it.printStackTrace()
            return Result.failure()
        }

        return Result.success(workDataOf(WORKER_LOCAL_MODEL_FILE_PATH_KEY to destinationFile.path))
    }

    companion object {
        internal const val WORKER_PROGRESS_KEY = "app.sarama.aeroedge.ModelDownloadWorker.PROGRESS"
        internal const val WORKER_LOCAL_MODEL_FILE_PATH_KEY =
            "app.sarama.aeroedge.ModelDownloadWorker.LOCAL_MODEL_FILE_PATH"

        internal const val KEY_MODEL_ENTITY_NAME = "app.sarama.aeroedge.NAME"
        internal const val KEY_MODEL_ENTITY_VERSION = "app.sarama.aeroedge.VERSION"
        internal const val KEY_MODEL_ENTITY_URL = "app.sarama.aeroedge.URL"
        internal const val KEY_MODEL_ENTITY_FILE_EXTENSION = "app.sarama.aeroedge.FILE_EXTENSION"


        private val Data.asModelEntity: kotlin.Result<ModelEntity>
            get() = runCatching {
                ModelEntity(
                    name = getString(KEY_MODEL_ENTITY_NAME)
                        ?: throw IllegalArgumentException("Missing $KEY_MODEL_ENTITY_NAME key in the WorkerData"),
                    version = getInt(KEY_MODEL_ENTITY_VERSION, -1).takeIf { it >= 0 }
                        ?: throw IllegalArgumentException("Missing $KEY_MODEL_ENTITY_VERSION key in the WorkerData"),
                    url = getString(KEY_MODEL_ENTITY_URL)
                        ?: throw IllegalArgumentException("Missing $KEY_MODEL_ENTITY_URL key in the WorkerData"),
                    fileExtension = getString(KEY_MODEL_ENTITY_FILE_EXTENSION)
                        ?: throw IllegalArgumentException("Missing $KEY_MODEL_ENTITY_FILE_EXTENSION key in the WorkerData"),
                )
            }
    }
}