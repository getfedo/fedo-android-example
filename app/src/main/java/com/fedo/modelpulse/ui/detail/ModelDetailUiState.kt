package com.fedo.modelpulse.ui.detail

import com.fedo.modelpulse.data.AiModel

sealed interface ModelDetailUiState {
    data object Loading : ModelDetailUiState

    /** The catalogue loaded, and this id is not in it. */
    data object NotFound : ModelDetailUiState

    data class Success(val model: AiModel) : ModelDetailUiState
}

/**
 * Pure: the cache plus the id in, screen state out. An empty cache is still
 * loading — the list is what fills it, and a cold start on the detail key
 * has nothing yet.
 */
internal fun toDetailUiState(models: List<AiModel>, id: String): ModelDetailUiState = when {
    models.isEmpty() -> ModelDetailUiState.Loading
    else -> models.firstOrNull { it.id == id }
        ?.let(ModelDetailUiState::Success)
        ?: ModelDetailUiState.NotFound
}
