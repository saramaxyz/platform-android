package app.sarama.aeroedge.di

import app.sarama.aeroedge.ui.screen.autocomplete.AutoCompleteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AutoCompleteViewModel(get()) }
}