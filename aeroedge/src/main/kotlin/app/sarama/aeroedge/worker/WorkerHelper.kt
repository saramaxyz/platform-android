package app.sarama.aeroedge.worker

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import app.sarama.aeroedge.Model
import app.sarama.aeroedge.ModelEntity
import app.sarama.aeroedge.ModelStatusUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.lang.IllegalStateException

suspend fun startModelDownloadInBackground(
    context: Context,
    model: ModelEntity
): Flow<ModelStatusUpdate> {
    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
        .setInputData(model.asWorkerData)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        model.workerName,
        ExistingWorkPolicy.KEEP, //Don't download the model if another worker is already doing it
        request,
    ).await()

    return channelFlow {
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(request.id)
            .asFlow()
            .collect {
                send(it.asModelStatusUpdate(model))
            }
    }
}

private val ModelEntity.asWorkerData: Data
    get() = Data
        .Builder()
        .putString(ModelDownloadWorker.KEY_MODEL_ENTITY_NAME, name)
        .putInt(ModelDownloadWorker.KEY_MODEL_ENTITY_VERSION, version)
        .putString(ModelDownloadWorker.KEY_MODEL_ENTITY_URL, url)
        .putString(ModelDownloadWorker.KEY_MODEL_ENTITY_FILE_EXTENSION, fileExtension)
        .build()

private val ModelEntity.workerName: String
    get() = "ModelDownloadWorker#$versionedNameWithExtension"

private fun WorkInfo.asModelStatusUpdate(modelEntity: ModelEntity): ModelStatusUpdate =
    when (state) {
        WorkInfo.State.RUNNING -> ModelStatusUpdate.InProgress(
            progress.getFloat(ModelDownloadWorker.WORKER_PROGRESS_KEY, 0.0F)
        )

        WorkInfo.State.SUCCEEDED -> {
            outputData
                .getString(ModelDownloadWorker.WORKER_LOCAL_MODEL_FILE_PATH_KEY)
                ?.let { filePath ->
                    println("[AEROEDGE] FOUND $filePath")
                    ModelStatusUpdate.OnCompleted(
                        model = Model(
                            modelName = modelEntity.name,
                            localFile = File(filePath),
                        ),
                        isModelUpToDate = true,
                    )
                }
                ?: ModelStatusUpdate.OnFailed(IllegalStateException("Missing ${ModelDownloadWorker.WORKER_LOCAL_MODEL_FILE_PATH_KEY} in the WorkInfo."))
        }

        else -> {
            ModelStatusUpdate.OnFailed(IllegalStateException("${state.javaClass.simpleName} not handled correctly"))
        }
    }