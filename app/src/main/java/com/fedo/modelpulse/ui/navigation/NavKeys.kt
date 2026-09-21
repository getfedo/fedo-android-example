package com.fedo.modelpulse.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ModelsKey : NavKey

@Serializable
data object RoadmapKey : NavKey

@Serializable
data object SettingsKey : NavKey

/** A bottom bar destination: the key it opens and the label it shows. */
enum class TopLevelDestination(val key: NavKey, val labelRes: Int) {
    MODELS(ModelsKey, com.fedo.modelpulse.R.string.nav_models),
    ROADMAP(RoadmapKey, com.fedo.modelpulse.R.string.nav_roadmap),
    SETTINGS(SettingsKey, com.fedo.modelpulse.R.string.nav_settings),
}

/**
 * The back stack after tapping [destination]. Models is always the root, so
 * system back from any other destination lands there instead of leaving the
 * app; tapping a destination drops whatever that section had pushed.
 */
fun backStackFor(destination: TopLevelDestination): List<NavKey> =
    if (destination == TopLevelDestination.MODELS) {
        listOf(ModelsKey)
    } else {
        listOf(ModelsKey, destination.key)
    }
