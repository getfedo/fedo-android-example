package com.fedo.modelpulse.ui.detail

import com.fedo.modelpulse.MainDispatcherRule
import com.fedo.modelpulse.TestData
import com.fedo.modelpulse.TestModelsRepository
import com.fedo.modelpulse.ui.navigation.ModelDetailKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ModelDetailViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = TestModelsRepository()

    @Test
    fun `AC-1 the key's model is the one on screen`() = runTest {
        repository.sendModels(TestData.testModels)

        val viewModel = ModelDetailViewModel(ModelDetailKey(TestData.llama.id), repository)

        val state = viewModel.uiState.first { it is ModelDetailUiState.Success }
        assertEquals(TestData.llama, (state as ModelDetailUiState.Success).model)
    }

    @Test
    fun `AC-4 an id missing from a loaded catalogue is not found`() = runTest {
        repository.sendModels(TestData.testModels)

        val viewModel = ModelDetailViewModel(ModelDetailKey("who/knows"), repository)

        assertEquals(
            ModelDetailUiState.NotFound,
            viewModel.uiState.first { it is ModelDetailUiState.NotFound },
        )
    }

    @Test
    fun `AC-4 an empty cache is still loading, not not-found`() {
        assertEquals(
            ModelDetailUiState.Loading,
            toDetailUiState(emptyList(), TestData.llama.id),
        )
    }
}
