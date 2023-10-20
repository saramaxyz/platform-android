package app.sarama.aeroedge.http

import app.sarama.aeroedge.ModelEntity
import app.sarama.aeroedge.ModelServerClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers

internal class AeroEdgeModelServerClient(
    private val apiKey: String,
    private val client: HttpClient = HttpClient(),
) : ModelServerClient {

    override suspend fun fetchRemoteModelInfo(modelName: String): Result<ModelEntity> {
        val response = client.get("$BaseUrl/models/$modelName/latest") {
            headers {
                append("apiKey", apiKey)
            }
        }

        if (response.status.value != 200) {
            return Result.failure(ModelError.NetworkError)
        }

        val apiResponse: ModelInfo = try {
            response.body()
        } catch (e: Throwable) {
            return Result.failure(
                ModelError.FailedToLoadModel(
                    e.message ?: "Failed to parse response"
                )
            )
        }

        return Result.success(
            ModelEntity(
                name = apiResponse.name,
                version = apiResponse.version,
                url = apiResponse.signedUrl,
                fileExtension = apiResponse.fileExtension,
            )
        )
    }

    private companion object {
        const val BaseUrl = "https://aeroedge-backend.fly.dev"
    }
}