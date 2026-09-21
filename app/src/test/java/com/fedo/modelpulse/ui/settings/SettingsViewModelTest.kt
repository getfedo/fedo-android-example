package com.fedo.modelpulse.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

    private val applied = mutableListOf<DemoUser?>()
    private var saved: DemoUser? = null
    private var savedCalls = 0

    private fun viewModel(
        isConfigured: Boolean = true,
        stored: DemoUser? = null,
    ) = SettingsViewModel(
        isConfigured = isConfigured,
        loadUser = { stored },
        saveUser = { user ->
            saved = user
            savedCalls++
        },
        applyIdentity = applied::add,
    )

    @Test
    fun `AC-1 signing in applies the demo user to the SDK`() {
        val viewModel = viewModel()

        viewModel.signIn()

        val user = viewModel.uiState.value.signedInAs
        assertNotNull(user)
        assertEquals(listOf(user), applied)
        assertEquals(DEFAULT_NAME, user?.name)
        assertEquals(DEFAULT_EMAIL, user?.email)
    }

    @Test
    fun `AC-2 signing out clears the SDK identity and the stored user`() {
        val viewModel = viewModel()
        viewModel.signIn()
        applied.clear()

        viewModel.signOut()

        assertEquals(listOf<DemoUser?>(null), applied)
        assertNull(saved)
        assertNull(viewModel.uiState.value.signedInAs)
    }

    @Test
    fun `AC-3 the demo user id is not the email`() {
        val viewModel = viewModel()

        viewModel.onEmailChange("someone@example.com")
        viewModel.signIn()

        val user = viewModel.uiState.value.signedInAs
        assertTrue(user!!.id.startsWith("demo-"))
        assertFalse(user.id.contains("@"))
    }

    @Test
    fun `AC-4 without a key sign-in is disabled and does nothing`() {
        val viewModel = viewModel(isConfigured = false)

        assertFalse(viewModel.uiState.value.canSignIn)
        viewModel.signIn()

        assertTrue(applied.isEmpty())
        assertEquals(0, savedCalls)
        assertNull(viewModel.uiState.value.signedInAs)
    }

    @Test
    fun `AC-5 a stored demo user is restored and re-applied on start`() {
        val stored = DemoUser("demo-abc", "Stored User", "stored@modelpulse.example")

        val viewModel = viewModel(stored = stored)

        assertEquals(stored, viewModel.uiState.value.signedInAs)
        assertEquals(listOf(stored), applied)
    }

    @Test
    fun `AC-5 a stored user is not pushed to an unconfigured SDK`() {
        val stored = DemoUser("demo-abc", "Stored User", "stored@modelpulse.example")

        val viewModel = viewModel(isConfigured = false, stored = stored)

        assertEquals(stored, viewModel.uiState.value.signedInAs)
        assertTrue(applied.isEmpty())
    }
}
