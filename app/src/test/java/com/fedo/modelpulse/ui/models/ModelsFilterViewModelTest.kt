package com.fedo.modelpulse.ui.models

import com.fedo.modelpulse.MainDispatcherRule
import com.fedo.modelpulse.TestData
import com.fedo.modelpulse.TestModelsRepository
import com.fedo.modelpulse.data.AiModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ModelsFilterViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = TestModelsRepository()
    private val allModels = TestData.testModels + TestData.claudeSonnet

    @Test
    fun `AC-1 search and provider filter combine`() = runTest {
        repository.sendModels(allModels)
        val viewModel = ModelsViewModel(repository)
        viewModel.uiState.first { it is ModelsUiState.Success }

        viewModel.onProviderChange("anthropic")
        viewModel.onQueryChange("sonnet")

        val state = viewModel.success { it.models.size == 1 }
        assertEquals(listOf("anthropic/claude-sonnet-5"), state.models.map(AiModel::id))
    }

    @Test
    fun `AC-2 the provider list counts models and shows each provider once`() = runTest {
        repository.sendModels(allModels)
        val viewModel = ModelsViewModel(repository)

        val state = viewModel.success { it.providers.isNotEmpty() }

        assertEquals(listOf("anthropic", "meta-llama"), state.providers.map { it.slug })
        assertEquals(listOf(2, 1), state.providers.map { it.count })
    }

    @Test
    fun `AC-3 a refresh that drops the selected provider clears the selection`() = runTest {
        repository.sendModels(allModels)
        val viewModel = ModelsViewModel(repository)
        viewModel.onProviderChange("meta-llama")
        viewModel.success { it.selectedProvider == "meta-llama" }

        repository.sendModels(listOf(TestData.claudeOpus))

        val state = viewModel.success { it.providers.size == 1 }
        assertNull(state.selectedProvider)
        assertFalse(state.isNoResults)
        assertEquals(listOf(TestData.claudeOpus), state.models)
    }

    @Test
    fun `AC-4 a search that matches nothing is no-results, not an empty catalogue`() = runTest {
        repository.sendModels(allModels)
        val viewModel = ModelsViewModel(repository)
        viewModel.uiState.first { it is ModelsUiState.Success }

        viewModel.onQueryChange("gemini")

        val state = viewModel.success { it.models.isEmpty() }
        assertTrue(state.isNoResults)
        // The providers are still there, which is what tells the screen apart
        // from an empty catalogue.
        assertTrue(state.providers.isNotEmpty())
    }

    private suspend fun ModelsViewModel.success(
        predicate: (ModelsUiState.Success) -> Boolean,
    ): ModelsUiState.Success =
        uiState.first { it is ModelsUiState.Success && predicate(it) } as ModelsUiState.Success
}
