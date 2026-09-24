package com.fedo.modelpulse.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation3.runtime.NavKey
import com.fedo.modelpulse.R
import com.fedo.modelpulse.ui.icons.IconAiFilled
import com.fedo.modelpulse.ui.icons.IconAiOutlined
import com.fedo.modelpulse.ui.icons.IconSettingsFilled
import com.fedo.modelpulse.ui.icons.IconSettingsOutlined
import com.fedo.modelpulse.ui.icons.IconTimeline
import kotlinx.serialization.Serializable

@Serializable
data object ModelsKey : NavKey

/** The argument holder for the detail screen; Koin injects it into the VM. */
@Serializable
data class ModelDetailKey(val id: String) : NavKey

@Serializable
data object RoadmapKey : NavKey

@Serializable
data object SettingsKey : NavKey

/** A bottom bar destination: the key it opens and the label it shows. */
enum class TopLevelDestination(
    val key: NavKey,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    MODELS(
        ModelsKey,
        R.string.nav_models,
        IconAiFilled,
        IconAiOutlined
    ),
//    ROADMAP(
//        RoadmapKey,
//        R.string.nav_roadmap,
//        IconTimeline,
//        IconTimeline
//    ),
    SETTINGS(
        SettingsKey,
        R.string.nav_settings,
        IconSettingsFilled,
        IconSettingsOutlined
    ),
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
