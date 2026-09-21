package com.fedo.modelpulse.data

import com.fedo.modelpulse.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelFilterTest {

    private val models = TestData.testModels + TestData.claudeSonnet

    @Test
    fun `AC-1 query matches the short name case-insensitively`() {
        val results = models.filterBy(query = "OPUS", providerSlugs = null)

        assertEquals(listOf("anthropic/claude-opus-5"), results.map(AiModel::id))
    }

    @Test
    fun `AC-1 query matches the id`() {
        val results = models.filterBy(query = "meta-llama/", providerSlugs = null)

        assertEquals(listOf("meta-llama/llama-4-8b:free"), results.map(AiModel::id))
    }

    @Test
    fun `AC-1 query and provider filter combine`() {
        val results = models.filterBy(query = "opus", providerSlugs = setOf("anthropic"))

        assertEquals(listOf("anthropic/claude-opus-5"), results.map(AiModel::id))
    }

    @Test
    fun `AC-1 a blank query and no provider match everything`() {
        assertEquals(models, models.filterBy(query = "   ", providerSlugs = null))
    }

    @Test
    fun `AC-2 ids sharing a display name merge into one entry`() {
        // OpenRouter ships both "meta/…" and "meta-llama/…" as "Meta".
        val meta = TestData.llama.copy(id = "meta/llama-guard", providerSlug = "meta")

        val filters = (models + meta).providerFilters()

        assertEquals(listOf("Anthropic", "Meta"), filters.map(ProviderFilter::name))
        val metaEntry = filters.first { it.name == "Meta" }
        assertEquals(setOf("meta", "meta-llama"), metaEntry.slugs)
        assertEquals(2, metaEntry.count)
        assertEquals("meta", metaEntry.key)
    }

    @Test
    fun `AC-2 a merged entry filters on every slug behind it`() {
        val meta = TestData.llama.copy(id = "meta/llama-guard", providerSlug = "meta")
        val all = models + meta

        val results = all.filterBy(query = "", providerSlugs = setOf("meta", "meta-llama"))

        assertEquals(2, results.size)
    }

    @Test
    fun `AC-2 each provider appears once, biggest first, with a count`() {
        val filters = models.providerFilters()

        assertEquals(
            listOf(
                ProviderFilter(setOf("anthropic"), "Anthropic", 2),
                ProviderFilter(setOf("meta-llama"), "Meta", 1),
            ),
            filters,
        )
    }
}
