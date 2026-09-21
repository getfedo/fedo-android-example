package com.fedo.modelpulse.di

import com.fedo.modelpulse.data.DefaultModelsRepository
import com.fedo.modelpulse.data.ModelsRepository
import com.fedo.modelpulse.data.OpenRouterDataSource
import com.fedo.modelpulse.ui.detail.ModelDetailViewModel
import com.fedo.modelpulse.ui.models.ModelsViewModel
import com.fedo.modelpulse.ui.navigation.ModelDetailKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // OpenRouter adds fields without warning, so unknown keys are ignored.
    single { Json { ignoreUnknownKeys = true } }
    single { OkHttpClient.Builder().build() }
    // Application-scoped: a refresh outlives the ViewModel that started it.
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    // Not singleOf: the data source's baseUrl is a test seam, not a graph type.
    single { OpenRouterDataSource(client = get(), json = get()) }
    singleOf(::DefaultModelsRepository) bind ModelsRepository::class
}

val uiModule = module {
    viewModelOf(::ModelsViewModel)
    // The nav key is the argument holder; Navigation 3 has no toRoute().
    viewModel { (key: ModelDetailKey) -> ModelDetailViewModel(key, get()) }
}
