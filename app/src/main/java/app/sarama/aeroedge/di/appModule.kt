package app.sarama.aeroedge.di

import app.sarama.aeroedge.AeroEdge
import app.sarama.aeroedge.ModelEntity
import app.sarama.aeroedge.ModelServerClient
import app.sarama.aeroedge.service.autocomplete.AutoCompleteService
import app.sarama.aeroedge.service.autocomplete.AutoCompleteServiceImpl
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext


val appModule = module {
    single<AutoCompleteService> {
        AutoCompleteServiceImpl(
            aeroEdge = get(),
        )
    }

    single {
        AeroEdge(
            context = androidContext(),
            client = object : ModelServerClient {
                override suspend fun fetchRemoteModelInfo(modelName: String) = Result.success(
                    ModelEntity(
                        name = modelName,
                        version = 1,
                        url = "https://gitlab.com/melvin.biamont/test-aeroedge/-/raw/main/autocomplete_1.tflite.zip?ref_type=heads&inline=false",
                        fileExtension = "tflite"
                    )
                )
            }
        )
    }
}