package com.fedo.modelpulse.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fedo.modelpulse.FedoIntegration
import com.fedo.modelpulse.data.ModelsRepository
import com.fedo.sdk.Fedo
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ModelsViewModel(
    private val repository: ModelsRepository,
    /**
     * Where the provider filter is reported to Fedo. Injected so the rule is
     * unit tested without the SDK, which is a singleton object.
     */
    private val reportFavoriteProvider: (String?) -> Unit = ::reportFavoriteProviderToFedo,
) : ViewModel() {

    private val loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    private val query = MutableStateFlow("")
    private val provider = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ModelsUiState> =
        combine(repository.models, query, provider, loadState, ::toUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ModelsUiState.Loading,
            )

    init {
        refresh()
    }

    /**
     * The repository supersedes any load already running, so tapping refresh
     * during the first load cannot leave two results racing.
     */
    fun refresh() {
        viewModelScope.launch {
            loadState.value =
                if (repository.models.value.isEmpty()) LoadState.Loading else LoadState.Refreshing

            loadState.value = repository.refresh().fold(
                onSuccess = { LoadState.Idle },
                onFailure = { LoadState.Failed(it.userMessage()) },
            )
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Null clears the filter. */
    fun onProviderChange(slug: String?) {
        provider.value = slug
        reportFavoriteProvider(slug)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Fedo user properties are user-level and last-write-wins, so the newest pick
 * is the whole story: there is no list to append to and no need to clear one
 * key before writing another. A cleared filter writes an empty value rather
 * than leaving the previous provider behind as the user's favourite.
 */
private fun reportFavoriteProviderToFedo(slug: String?) {
    if (!FedoIntegration.isConfigured) return

    Fedo.setUserProperty(FAVORITE_PROVIDER, slug.orEmpty())
}

private const val FAVORITE_PROVIDER = "favorite_provider"

/**
 * The one place a throwable becomes something a person reads. The data layer
 * deliberately does not do this.
 */
internal fun Throwable.userMessage(): String = when (this) {
    is IOException -> "Couldn't reach OpenRouter. Check your connection."
    else -> "Something went wrong loading the catalogue."
}
