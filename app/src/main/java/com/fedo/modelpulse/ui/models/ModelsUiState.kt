package com.fedo.modelpulse.ui.models

import com.fedo.modelpulse.data.AiModel

/** Every state the models list can be in. */
sealed interface ModelsUiState {

    data object Loading : ModelsUiState

    /** Nothing to show. A failed refresh with data present is not this. */
    data class Error(val message: String) : ModelsUiState

    data class Success(
        val models: List<AiModel>,
        val isRefreshing: Boolean = false,
        /** Set when a refresh failed while data was already on screen. */
        val refreshError: String? = null,
    ) : ModelsUiState
}

/** Where a load is, independent of what data is cached. */
internal sealed interface LoadState {
    data object Loading : LoadState
    data object Refreshing : LoadState
    data object Idle : LoadState
    data class Failed(val message: String) : LoadState
}

/**
 * Pure: cached models plus load state in, screen state out. This is what the
 * unit tests call.
 */
internal fun toUiState(models: List<AiModel>, load: LoadState): ModelsUiState = when {
    models.isNotEmpty() -> ModelsUiState.Success(
        models = models,
        isRefreshing = load is LoadState.Refreshing,
        refreshError = (load as? LoadState.Failed)?.message,
    )

    load is LoadState.Failed -> ModelsUiState.Error(load.message)
    load is LoadState.Loading || load is LoadState.Refreshing -> ModelsUiState.Loading
    // Loaded, and the catalogue really is empty.
    else -> ModelsUiState.Success(models = emptyList())
}
