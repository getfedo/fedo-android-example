package com.fedo.modelpulse.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class NavKeysTest {

    @Test
    fun `AC-1 every destination has a key and a label`() {
        assertEquals(
            listOf(ModelsKey, RoadmapKey, SettingsKey),
            TopLevelDestination.entries.map(TopLevelDestination::key),
        )
        assertEquals(3, TopLevelDestination.entries.count { it.labelRes != 0 })
    }

    @Test
    fun `AC-2 back from another destination returns to Models`() {
        assertEquals(listOf(ModelsKey, RoadmapKey), backStackFor(TopLevelDestination.ROADMAP))
        assertEquals(listOf(ModelsKey, SettingsKey), backStackFor(TopLevelDestination.SETTINGS))
    }

    @Test
    fun `AC-2 Models is the root, so back from it leaves the app`() {
        assertEquals(listOf(ModelsKey), backStackFor(TopLevelDestination.MODELS))
    }
}
