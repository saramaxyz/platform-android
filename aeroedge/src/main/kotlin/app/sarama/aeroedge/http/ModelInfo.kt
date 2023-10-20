package app.sarama.aeroedge.http

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ModelInfo(
    @SerialName("name") val name: String,
    @SerialName("signed_url") val signedUrl: String,
    @SerialName("version") val version: Int,
)

internal val ModelInfo.fileExtension: String
    get() = Url(signedUrl).pathSegments
        .lastOrNull()
        ?.split(".")
        ?.drop(1)
        ?.joinToString(".")
        ?: ""