package app.sarama.aeroedge

data class ModelEntity(
    val name: String,
    val version: Int,
    val url: String,
    val fileExtension: String,
) {

    internal val versionedName: String
        get() = "${name}_$version"

    internal val versionedNameWithExtension: String
        get() = "${versionedName}.$fileExtension"
}
