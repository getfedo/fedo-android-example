package com.fedo.modelpulse.data

import com.fedo.modelpulse.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelFilterTest {

    private val models = TestData.testModels + TestData.claudeSonnet

    @Test
    fun `AC-1 query matches the short name case-insensitively`() {
        val results = models.filterBy(query = "OPUS", providerSlug = null)

        assertEquals(listOf("anthropic/claude-opus-5"), results.map(AiModel::id))
    }

    @Test
    fun `AC-1 query matches the id`() {
        val results = models.filterBy(query = "meta-llama/", providerSlug = null)

        assertEquals(listOf("meta-llama/llama-4-8b:free"), results.map(AiModel::id))
    }

    @Test
    fun `AC-1 query and provider filter combine`() {
        val results = models.filterBy(query = "opus", providerSlug = "anthropic")

        assertEquals(listOf("anthropic/claude-opus-5"), results.map(AiModel::id))
    }

    @Test
    fun `AC-1 a blank query and no provider match everything`() {
        assertEquals(models, models.filterBy(query = "   ", providerSlug = null))
    }

    @Test
    fun `AC-2 each provider appears once, biggest first, with a count`() {
        val filters = models.providerFilters()

        assertEquals(
            listOf(
                ProviderFilter("anthropic", "Anthropic", 2),
                ProviderFilter("meta-llama", "Meta", 1),
            ),
            filters,
        )
    }
}
