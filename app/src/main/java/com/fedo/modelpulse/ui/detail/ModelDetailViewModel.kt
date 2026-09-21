package com.fedo.modelpulse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fedo.modelpulse.data.ModelsRepository
import com.fedo.modelpulse.ui.navigation.ModelDetailKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ModelDetailViewModel(
    key: ModelDetailKey,
    repository: ModelsRepository,
) : ViewModel() {

    val uiState: StateFlow<ModelDetailUiState> =
        repository.models
            .map { models -> toDetailUiState(models, key.id) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ModelDetailUiState.Loading,
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
