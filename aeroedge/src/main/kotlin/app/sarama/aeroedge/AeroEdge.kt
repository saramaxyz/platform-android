package app.sarama.aeroedge

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import app.sarama.aeroedge.http.AeroEdgeModelServerClient
import app.sarama.aeroedge.http.ModelError
import app.sarama.aeroedge.worker.startModelDownloadInBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface AeroEdge {

    suspend fun getModel(modelName: String, modelType: ModelType): Flow<ModelStatusUpdate>

}

fun AeroEdge(context: Context, apiKey: String): AeroEdge =
    AeroEdge(context, AeroEdgeModelServerClient(apiKey))

fun AeroEdge(context: Context, client: ModelServerClient): AeroEdge = AeroEdgeImpl(context, client)

internal class AeroEdgeImpl(
    private val context: Context,
    private val client: ModelServerClient,
    private val localStore: ModelLocalStore = DefaultModelLocalStore(context),
) : AeroEdge {

    override suspend fun getModel(modelName: String, modelType: ModelType) = flow {
        // We fetch remote model information
        val remoteModelInfo = client.fetchRemoteModelInfo(modelName).getOrElse {
            //An error occurred while fetching remote model information
            emit(ModelStatusUpdate.OnFailed(it))
            return@flow
        }

        // We verify which latest version of the model we have in local.
        // If no version of the model is found, it'll just return null
        localStore.getLocalModelVersion(modelName, remoteModelInfo.fileExtension).getOrElse {
            // An error occurred while trying to find the model files
            emit(ModelStatusUpdate.OnFailed(it))
            return@flow
        }?.let { localVersion ->
            // We have a local version for the model
            val isModelUpToDate = localVersion >= remoteModelInfo.version

            // We get the local model file
            val localModelFile = localStore.getLocalModelFile(
                modelName,
                localVersion,
                modelType.extensionName,
            ) ?: run {
                //An error occurred when trying to find the local model file
                emit(ModelStatusUpdate.OnFailed(ModelError.FileNotFound))
                return@flow
            }

            emit(
                ModelStatusUpdate.OnCompleted(
                    model = Model(
                        modelName = modelName,
                        localFile = localModelFile,
                    ),
                    isModelUpToDate = isModelUpToDate,
                )
            )

            if (isModelUpToDate) {
                //We already have the latest version, no need to continue
                return@flow
            }
        }

        emitAll(startModelDownloadInBackground(context, remoteModelInfo))
    }
}