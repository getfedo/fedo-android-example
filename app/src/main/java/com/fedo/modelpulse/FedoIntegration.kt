package com.fedo.modelpulse

/**
 * Whether the build carries a Fedo API key. The key is injected from the
 * gitignored local.properties; a clone without one still builds and runs, it
 * just does not show the Fedo surfaces.
 */
object FedoIntegration {
    val isConfigured: Boolean = isFedoConfigured(BuildConfig.FEDO_API_KEY)
}

internal fun isFedoConfigured(apiKey: String): Boolean = apiKey.isNotBlank()
