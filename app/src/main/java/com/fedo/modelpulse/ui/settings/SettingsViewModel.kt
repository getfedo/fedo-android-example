package com.fedo.modelpulse.ui.settings

import androidx.lifecycle.ViewModel
import com.fedo.sdk.Fedo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val isConfigured: Boolean,
    val signedInAs: DemoUser? = null,
    val name: String = DEFAULT_NAME,
    val email: String = DEFAULT_EMAIL,
) {
    /** Sign-in needs a key and both fields; without a key the SDK ignores us. */
    val canSignIn: Boolean
        get() = isConfigured && signedInAs == null && name.isNotBlank() && email.isNotBlank()
}

const val DEFAULT_NAME = "Demo User"
const val DEFAULT_EMAIL = "demo@modelpulse.example"

/**
 * [applyIdentity] is the only place the SDK is touched: it gets the new user,
 * or null on sign-out. Passing it in keeps the sign-in rules testable without
 * the SDK, which is a singleton object.
 */
class SettingsViewModel(
    isConfigured: Boolean,
    private val loadUser: () -> DemoUser?,
    private val saveUser: (DemoUser?) -> Unit,
    private val applyIdentity: (DemoUser?) -> Unit = ::applyToFedo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isConfigured = isConfigured))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // A demo user saved by an earlier run is re-applied, so the SDK and the
        // screen agree after a restart.
        loadUser()?.let { user ->
            _uiState.update { it.copy(signedInAs = user, name = user.name, email = user.email) }
            if (isConfigured) applyIdentity(user)
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }

    fun signIn() {
        val state = _uiState.value
        if (!state.canSignIn) return

        val user = DemoUser.create(state.name.trim(), state.email.trim())
        applyIdentity(user)
        saveUser(user)
        _uiState.update { it.copy(signedInAs = user) }
    }

    fun signOut() {
        if (_uiState.value.signedInAs == null) return

        applyIdentity(null)
        saveUser(null)
        _uiState.update { it.copy(signedInAs = null, name = DEFAULT_NAME, email = DEFAULT_EMAIL) }
    }
}

/** Sign-in is three calls; sign-out is one. That is the whole identity API. */
private fun applyToFedo(user: DemoUser?) {
    if (user == null) {
        Fedo.logout()
    } else {
        Fedo.setUserID(user.id)
        Fedo.setUserDisplayName(user.name)
        Fedo.setUserEmail(user.email)
    }
}
