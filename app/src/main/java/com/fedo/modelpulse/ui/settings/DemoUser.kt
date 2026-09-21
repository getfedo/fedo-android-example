package com.fedo.modelpulse.ui.settings

import android.content.Context
import java.util.UUID

/**
 * The fake account the Settings screen signs in as. [id] is deliberately not
 * the email: a real app passes its own backend user id here and keeps PII out
 * of it — Fedo gets the email through `setUserEmail`, where it belongs.
 */
data class DemoUser(
    val id: String,
    val name: String,
    val email: String,
) {
    companion object {
        fun create(name: String, email: String): DemoUser =
            DemoUser(id = "demo-" + UUID.randomUUID(), name = name, email = email)
    }
}

/**
 * Keeps the demo user across restarts, so signing in once survives the app
 * being killed. SharedPreferences, not DataStore — see
 * specs/decisions/0004-demo-user-persistence.md.
 */
class DemoUserStore(context: Context) {

    private val prefs = context.getSharedPreferences("demo_user", Context.MODE_PRIVATE)

    fun load(): DemoUser? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        return DemoUser(
            id = id,
            name = prefs.getString(KEY_NAME, "").orEmpty(),
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
        )
    }

    fun save(user: DemoUser?) {
        prefs.edit().run {
            if (user == null) {
                clear()
            } else {
                putString(KEY_ID, user.id)
                putString(KEY_NAME, user.name)
                putString(KEY_EMAIL, user.email)
            }
            apply()
        }
    }

    private companion object {
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
    }
}
