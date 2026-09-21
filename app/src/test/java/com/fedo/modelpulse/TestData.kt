package com.fedo.modelpulse

import com.fedo.modelpulse.data.AiModel
import com.fedo.modelpulse.data.Price
import java.math.BigDecimal
import java.time.Instant

object TestData {

    val claudeOpus = AiModel(
        id = "anthropic/claude-opus-5",
        providerSlug = "anthropic",
        shortName = "Claude Opus 5",
        providerName = "Anthropic",
        created = Instant.ofEpochSecond(1_756_000_000),
        description = "Most capable Claude model.",
        contextLength = 200_000,
        promptPrice = Price.PerToken(BigDecimal("0.000015")),
        completionPrice = Price.PerToken(BigDecimal("0.000075")),
        inputModalities = listOf("text", "image"),
    )

    val llama = claudeOpus.copy(
        id = "meta-llama/llama-4-8b:free",
        providerSlug = "meta-llama",
        shortName = "Llama 4 8B",
        providerName = "Meta",
        created = Instant.ofEpochSecond(1_713_000_000),
        contextLength = null,
        promptPrice = Price.Free,
        completionPrice = Price.Free,
    )

    /** Same provider slug as [claudeOpus], so the two merge into one filter. */
    val claudeSonnet = claudeOpus.copy(
        id = "anthropic/claude-sonnet-5",
        shortName = "Claude Sonnet 5",
        created = Instant.ofEpochSecond(1_750_000_000),
    )

    /** Newest first, the order the data source promises. */
    val testModels = listOf(claudeOpus, llama)
}
