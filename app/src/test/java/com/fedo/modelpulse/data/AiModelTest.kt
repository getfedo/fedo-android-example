package com.fedo.modelpulse.data

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The money path and the derived fields. Broader coverage (decoding a real
 * payload, filtering) belongs to its.2.
 */
class AiModelTest {

    @Test
    fun `AC-1 a price that will not parse is unknown, not variable`() {
        assertEquals(Price.Unknown, Price.parse("cheap"))
        assertEquals(Price.Unknown, Price.parse(""))
        assertEquals("—", Price.Unknown.perMillionLabel())
    }

    @Test
    fun `AC-2 minus one is still variable`() {
        assertEquals(Price.Variable, Price.parse("-1"))
        assertEquals("Variable", Price.Variable.perMillionLabel())
    }

    @Test
    fun `price per million renders the documented examples`() {
        assertEquals("$0.96", Price.parse("0.00000096").perMillionLabel())
        assertEquals("$0.075", Price.parse("0.000000075").perMillionLabel())
        assertEquals("$15", Price.parse("0.000015").perMillionLabel())
        assertEquals("Free", Price.parse("0").perMillionLabel())
        assertEquals("Variable", Price.parse("-1").perMillionLabel())
        assertEquals("—", Price.parse("cheap").perMillionLabel())
        assertEquals("—", Price.parse("").perMillionLabel())
    }

    @Test
    fun `a price above zero but under a cent never renders as free`() {
        assertEquals("<$0.01", Price.parse("0.000000001").perMillionLabel())
    }

    @Test
    fun `context length is labelled in K and M`() {
        assertEquals("1M", 1_048_576.contextLabel())
        assertEquals("200K", 200_000.contextLabel())
        assertEquals("131K", 131_072.contextLabel())
        assertEquals("32K", 32_768.contextLabel())
        assertEquals("512", 512.contextLabel())
        assertEquals("—", null.contextLabel())
    }

    @Test
    fun `mapping strips the tilde and derives the provider from the name`() {
        val model = NetworkModel(
            id = "~anthropic/claude-opus-5",
            name = "Anthropic: Claude Opus 5",
            created = 1_756_000_000L,
            pricing = NetworkPricing(prompt = "0.000015", completion = "0.000075"),
        ).asExternalModel()

        assertEquals("anthropic/claude-opus-5", model.id)
        assertEquals("anthropic", model.providerSlug)
        assertEquals("Anthropic", model.providerName)
        assertEquals("Claude Opus 5", model.shortName)
        assertEquals(Instant.ofEpochSecond(1_756_000_000L), model.created)
    }

    @Test
    fun `a name without a provider prefix falls back to the id prefix`() {
        val model = NetworkModel(id = "openai/gpt-5", name = "GPT-5").asExternalModel()

        assertEquals("openai", model.providerName)
        assertEquals("GPT-5", model.shortName)
        assertEquals(null, model.contextLength)
        // No pricing block on the wire: unknown, not variable.
        assertEquals(Price.Unknown, model.promptPrice)
    }

    @Test
    fun `release date reads as a relative label`() {
        val now = Instant.parse("2026-09-21T00:00:00Z")

        assertEquals("today", now.relativeLabel(now))
        assertEquals("yesterday", now.minus(1, ChronoUnit.DAYS).relativeLabel(now))
        assertEquals("3 days ago", now.minus(3, ChronoUnit.DAYS).relativeLabel(now))
        assertEquals("2 months ago", now.minus(70, ChronoUnit.DAYS).relativeLabel(now))
        assertEquals("1 year ago", now.minus(400, ChronoUnit.DAYS).relativeLabel(now))
    }
}
