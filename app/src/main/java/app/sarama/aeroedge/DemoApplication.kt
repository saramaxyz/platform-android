package app.sarama.aeroedge

import android.app.Application
import app.sarama.aeroedge.di.appModule
import app.sarama.aeroedge.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@DemoApplication)

            modules(appModule, viewModelModule)
        }
    }
}