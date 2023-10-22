package app.sarama.aeroedge.http

import app.sarama.aeroedge.FileDownloadState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile


internal interface ModelDownloaderClient {

    suspend fun downloadFile(url: String, destinationFile: File): Flow<FileDownloadState>
}

internal class DefaultModelDownloaderClient(
    private val client: HttpClient = HttpClient(),
) : ModelDownloaderClient {

    override suspend fun downloadFile(url: String, destinationFile: File) = flow {
        val parent = destinationFile.parentFile ?: run {
            emit(FileDownloadState.OnFailed(ModelError.FileWriteError))
            return@flow
        }
        val zipFile = File(parent, "${destinationFile.name}.zip")

        downloadZipFile(url, zipFile).getOrElse { return@flow emit(FileDownloadState.OnFailed(it)) }

        println("[AEROEDGE] downloaded ${zipFile.path}")

        unzipModelFile(
            zipFile,
            destinationFile
        ).getOrElse { return@flow emit(FileDownloadState.OnFailed(it)) }

        println("[AEROEDGE] unzipped into ${destinationFile.path}")

        emit(FileDownloadState.OnSuccess)
    }

    private suspend fun FlowCollector<FileDownloadState>.downloadZipFile(
        url: String,
        zipFile: File,
    ): Result<Unit> = runCatching {
        client.prepareGet(url).execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.body()
            val totalSize = httpResponse.contentLength()
            var isFileDownloaded = false
            var downloadedSize = 0L

            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    zipFile.appendBytes(bytes)

                    downloadedSize += bytes.size
                    totalSize?.let {
                        val progress = downloadedSize.toFloat() / totalSize.toFloat()
                        emit(FileDownloadState.OnProgress(progress))
                    }
                }
                isFileDownloaded = true
            }

            if (!isFileDownloaded) {
                throw ModelError.NetworkError
            }
        }
    }

    private fun unzipModelFile(
        zipFile: File,
        destinationFile: File,
    ): Result<Unit> = runCatching<Unit> {
        var found = false
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.name != destinationFile.name) return@forEach

                zip.getInputStream(entry).use { inputStream ->
                    val bos = BufferedOutputStream(FileOutputStream(destinationFile))
                    val bytesIn = ByteArray(1024)
                    var read: Int
                    while (inputStream.read(bytesIn).also { read = it } != -1) {
                        bos.write(bytesIn, 0, read)
                    }
                    bos.close()
                    found = true
                }
            }
        }
        zipFile.delete()

        if(!found) {
            throw IllegalStateException("No entry found with the name ${destinationFile.name} into ${zipFile.name}")
        }
    }

}