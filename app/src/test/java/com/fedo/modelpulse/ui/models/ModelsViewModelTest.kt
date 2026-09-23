package com.fedo.modelpulse.ui.models

import com.fedo.modelpulse.MainDispatcherRule
import com.fedo.modelpulse.R
import com.fedo.modelpulse.TestData
import com.fedo.modelpulse.TestModelsRepository
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ModelsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = TestModelsRepository()

    @Test
    fun `AC-1 models reach the screen in the order the repository holds them`() = runTest {
        repository.sendModels(TestData.testModels)

        val viewModel = ModelsViewModel(repository)

        // first() subscribes, which is what starts the WhileSubscribed share.
        val state = viewModel.uiState.first { it is ModelsUiState.Success }

        assertEquals(TestData.testModels, (state as ModelsUiState.Success).models)
    }

    @Test
    fun `AC-2 a first load that fails with nothing cached is an error state`() = runTest {
        repository.refreshResult = Result.failure(IOException("offline"))

        val viewModel = ModelsViewModel(repository)

        val state = viewModel.uiState.first { it is ModelsUiState.Error }
        assertTrue(state is ModelsUiState.Error)
        assertEquals(R.string.models_error_offline, (state as ModelsUiState.Error).messageRes)
    }

    @Test
    fun `AC-3 a failed refresh keeps the list and reports inline`() = runTest {
        val viewModel = ModelsViewModel(repository)
        repository.sendModels(TestData.testModels)
        val loaded = viewModel.uiState.first {
            it is ModelsUiState.Success && it.models.isNotEmpty()
        }
        assertNull((loaded as ModelsUiState.Success).refreshErrorRes)

        repository.refreshResult = Result.failure(IOException("offline"))
        viewModel.refresh()

        val state = viewModel.uiState.first {
            it is ModelsUiState.Success && it.refreshErrorRes != null
        } as ModelsUiState.Success
        assertEquals(TestData.testModels, state.models)
        assertNotNull(state.refreshErrorRes)
    }

    @Test
    fun `AC-4 an empty catalogue is success with no models, not an error`() = runTest {
        val viewModel = ModelsViewModel(repository)

        val state = viewModel.uiState.first { it is ModelsUiState.Success }
        assertTrue((state as ModelsUiState.Success).models.isEmpty())
    }

    @Test
    fun `uiState is Loading until the first load settles`() = runTest {
        assertEquals(ModelsUiState.Loading, toUiState(emptyList(), query = "", provider = null, load = LoadState.Loading))
    }

    @Test
    fun `a refresh with data on screen sets isRefreshing rather than Loading`() = runTest {
        val state = toUiState(TestData.testModels, query = "", provider = null, load = LoadState.Refreshing)

        assertTrue((state as ModelsUiState.Success).isRefreshing)
    }
}
