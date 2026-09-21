package com.fedo.modelpulse

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FedoIntegrationTest {

    @Test
    fun `AC-1 a key marks Fedo as configured`() {
        assertTrue(isFedoConfigured("fedo_live_abc123"))
    }

    @Test
    fun `AC-2 a missing key leaves Fedo unconfigured`() {
        assertFalse(isFedoConfigured(""))
    }

    @Test
    fun `AC-2 a whitespace-only key leaves Fedo unconfigured`() {
        assertFalse(isFedoConfigured("   "))
    }
}
