package com.fedo.modelpulse.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * One entry of the OpenRouter catalogue, in the shape the UI wants. Everything
 * derived from the wire format is computed once here, in [asExternalModel], so
 * composables never parse or format.
 */
data class AiModel(
    val id: String,
    /** Lowercase grouping key, e.g. "anthropic". Stable across name changes. */
    val providerSlug: String,
    val shortName: String,
    val providerName: String,
    val created: Instant,
    val description: String,
    val contextLength: Int?,
    val promptPrice: Price,
    val completionPrice: Price,
    val inputModalities: List<String>,
)

/** USD price for one token, as OpenRouter reports it (a string). */
sealed interface Price {
    data object Free : Price
    data object Variable : Price
    data class PerToken(val usd: BigDecimal) : Price

    companion object {
        fun parse(raw: String): Price {
            val value = raw.trim().toBigDecimalOrNull() ?: return Variable
            return when {
                value.signum() == 0 -> Free
                value.signum() < 0 -> Variable
                else -> PerToken(value)
            }
        }
    }
}

private val ONE_MILLION = BigDecimal(1_000_000)
private val ONE_CENT = BigDecimal("0.01")

/**
 * Price for a million tokens: "$15", "$0.96", "$0.075", "Free", "Variable".
 * Anything above zero but under a cent is "<$0.01" — never "$0", which would
 * read as free.
 */
fun Price.perMillionLabel(): String = when (this) {
    Price.Free -> "Free"
    Price.Variable -> "Variable"
    is Price.PerToken -> {
        val perMillion = usd.multiply(ONE_MILLION).setScale(4, RoundingMode.HALF_UP)
        if (perMillion < ONE_CENT) "<$0.01"
        else "$" + perMillion.stripTrailingZeros().toPlainString()
    }
}

/** Context window as a short label: "1M", "200K", "512", "—" when unknown. */
fun Int?.contextLabel(): String = when {
    this == null -> "—"
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

/**
 * How long ago the model was released: "today", "3 days ago", "2 months ago".
 * [now] is a parameter so tests do not depend on the wall clock.
 */
fun Instant.relativeLabel(now: Instant = Instant.now()): String {
    val days = ChronoUnit.DAYS.between(this, now)
    return when {
        days < 0L -> "today" // ponytail: clock skew, not worth a "in the future" branch
        days == 0L -> "today"
        days == 1L -> "yesterday"
        days < 30L -> "$days days ago"
        days < 365L -> (days / 30).let { if (it == 1L) "1 month ago" else "$it months ago" }
        else -> (days / 365).let { if (it == 1L) "1 year ago" else "$it years ago" }
    }
}
