package com.fedo.modelpulse.ui.models

import androidx.annotation.StringRes
import com.fedo.modelpulse.data.AiModel
import com.fedo.modelpulse.data.ProviderFilter
import com.fedo.modelpulse.data.filterBy
import com.fedo.modelpulse.data.providerFilters

/** Every state the models list can be in. */
sealed interface ModelsUiState {

    data object Loading : ModelsUiState

    /** Nothing to show. A failed refresh with data present is not this. */
    data class Error(@param:StringRes val messageRes: Int) : ModelsUiState

    data class Success(
        /** What the search and provider filter left. */
        val models: List<AiModel>,
        val query: String = "",
        val providers: List<ProviderFilter> = emptyList(),
        /** Null when no provider is picked, or when the picked one is gone. */
        val selectedProvider: String? = null,
        val isRefreshing: Boolean = false,
        /** Set when a refresh failed while data was already on screen. */
        @param:StringRes val refreshErrorRes: Int? = null,
    ) : ModelsUiState {

        val isFiltered: Boolean get() = query.isNotBlank() || selectedProvider != null

        /** Empty because of the filters, not because the catalogue is empty. */
        val isNoResults: Boolean get() = models.isEmpty() && isFiltered
    }
}

/** Where a load is, independent of what data is cached. */
internal sealed interface LoadState {
    data object Loading : LoadState
    data object Refreshing : LoadState
    data object Idle : LoadState
    data class Failed(@param:StringRes val messageRes: Int) : LoadState
}

/**
 * Pure: cached models, search, provider and load state in, screen state out.
 * This is what the unit tests call.
 */
internal fun toUiState(
    models: List<AiModel>,
    query: String,
    provider: String?,
    load: LoadState,
): ModelsUiState = when {
    models.isNotEmpty() -> {
        val providers = models.providerFilters()
        // A refresh that drops the provider drops the selection with it, so the
        // filter can never strand the screen on a provider that no longer exists.
        val selectedEntry = providers.firstOrNull { it.key == provider }

        ModelsUiState.Success(
            models = models.filterBy(query, selectedEntry?.slugs),
            query = query,
            providers = providers,
            selectedProvider = selectedEntry?.key,
            isRefreshing = load is LoadState.Refreshing,
            refreshErrorRes = (load as? LoadState.Failed)?.messageRes,
        )
    }

    load is LoadState.Failed -> ModelsUiState.Error(load.messageRes)
    load is LoadState.Loading || load is LoadState.Refreshing -> ModelsUiState.Loading
    // Loaded, and the catalogue really is empty.
    else -> ModelsUiState.Success(models = emptyList())
}
