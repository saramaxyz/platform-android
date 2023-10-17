package app.sarama.aeroedge

interface ModelServerClient {

    suspend fun fetchRemoteModelInfo(modelName: String): Result<ModelEntity>
}