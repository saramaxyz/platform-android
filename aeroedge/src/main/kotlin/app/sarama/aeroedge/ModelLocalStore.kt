package app.sarama.aeroedge

import android.content.Context
import java.io.File

internal interface ModelLocalStore {

    suspend fun getLocalModelVersion(modelName: String, fileExtension: String): Result<Int?>

    suspend fun getLocalModelFile(modelName: String, version: Int, fileExtension: String): File?

    suspend fun createLocalModelFile(modelName: String, version: Int, fileExtension: String): File

    suspend fun deleteOldVersions(model: ModelEntity): Result<Unit>
}

internal class DefaultModelLocalStore(
    private val context: Context,
    private val modelDirectoryName: String = DefaultModelDirectoryName
) : ModelLocalStore {

    private val modelsDirectory: File by lazy {
        context.getDir(
            modelDirectoryName,
            Context.MODE_PRIVATE
        )
    }

    override suspend fun getLocalModelVersion(
        modelName: String,
        fileExtension: String,
    ) = runCatching {
        val regex = getModelFileRegex(modelName)

        modelsDirectory
            .listFiles()
            ?.filter { it.nameWithoutExtension.matches(regex) }
            ?.mapNotNull { it.version }
            ?.maxOfOrNull { it }
    }

    override suspend fun getLocalModelFile(
        modelName: String,
        version: Int,
        fileExtension: String
    ) = File(modelsDirectory, "${modelName}_${version}.$fileExtension").takeIf { it.exists() }

    override suspend fun createLocalModelFile(
        modelName: String,
        version: Int,
        fileExtension: String
    ) = File(modelsDirectory, "${modelName}_${version}.$fileExtension")

    override suspend fun deleteOldVersions(model: ModelEntity) = runCatching {
        val regex = getModelFileRegex(model.name)

        modelsDirectory
            .listFiles()
            ?.filter { it.nameWithoutExtension.matches(regex) }
            ?.filter { (it.version ?: Int.MAX_VALUE) <= model.version }
            ?.forEach { it.delete() }
            ?: Unit
    }

    private fun getModelFileRegex(modelName: String): Regex = "${modelName}_[0-9]+".toRegex()

    private val File.version: Int?
        get() = nameWithoutExtension.split("_").lastOrNull()?.toInt()

    private companion object {
        const val DefaultModelDirectoryName = "models"
    }

}