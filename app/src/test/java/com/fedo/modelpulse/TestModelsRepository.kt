package com.fedo.modelpulse

import com.fedo.modelpulse.data.AiModel
import com.fedo.modelpulse.data.ModelsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A real implementation with test hooks, not a mock. A failing refresh keeps
 * whatever [sendModels] already emitted — the behaviour AC-3 depends on.
 */
class TestModelsRepository : ModelsRepository {

    private val _models = MutableStateFlow(emptyList<AiModel>())
    override val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    var refreshResult: Result<Unit> = Result.success(Unit)
    var refreshCount = 0
        private set

    fun sendModels(models: List<AiModel>) {
        _models.value = models
    }

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        return refreshResult
    }
}
