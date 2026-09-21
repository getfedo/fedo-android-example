package com.fedo.modelpulse.data

import com.fedo.modelpulse.TestData
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class TestOpenRouterDataSource : OpenRouterDataSource() {

    var response: Result<List<AiModel>> = Result.success(emptyList())
    var gate: CompletableDeferred<Unit>? = null
    var calls = 0
        private set

    override suspend fun getModels(): Result<List<AiModel>> {
        calls++
        gate?.await()
        return response
    }
}

class ModelsRepositoryTest {

    private val remote = TestOpenRouterDataSource()

    @Test
    fun `a successful refresh publishes what the data source returned`() = runTest {
        val repository = DefaultModelsRepository(remote, TestScope(testScheduler))
        remote.response = Result.success(TestData.testModels)

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        assertEquals(TestData.testModels, repository.models.value)
    }

    @Test
    fun `AC-3 a failed refresh keeps the models already published`() = runTest {
        val repository = DefaultModelsRepository(remote, TestScope(testScheduler))
        remote.response = Result.success(TestData.testModels)
        repository.refresh()

        remote.response = Result.failure(IOException("offline"))
        val result = repository.refresh()

        assertTrue(result.isFailure)
        assertEquals(TestData.testModels, repository.models.value)
    }

    @Test
    fun `AC-5 a refresh started during a load joins it instead of fetching twice`() = runTest {
        val repository = DefaultModelsRepository(remote, TestScope(testScheduler))
        val gate = CompletableDeferred<Unit>()
        remote.gate = gate
        remote.response = Result.success(TestData.testModels)

        // Two callers, one load: the first parks on the gate, the second
        // arrives while it is still running.
        val first = async { repository.refresh() }
        testScheduler.advanceUntilIdle()
        val second = async { repository.refresh() }
        testScheduler.advanceUntilIdle()

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, remote.calls)
        // Neither caller is left with a cancellation instead of a result.
        assertTrue(first.await().isSuccess)
        assertTrue(second.await().isSuccess)
        assertEquals(TestData.testModels, repository.models.value)
    }

    @Test
    fun `AC-5 a refresh after a load finished starts a fresh one`() = runTest {
        val repository = DefaultModelsRepository(remote, TestScope(testScheduler))
        remote.response = Result.success(TestData.testModels)

        repository.refresh()
        repository.refresh()

        assertEquals(2, remote.calls)
    }
}
