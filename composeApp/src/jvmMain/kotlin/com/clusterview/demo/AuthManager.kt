package com.clusterview.demo

import java.util.prefs.Preferences

object AuthManager {
    private val prefs = Preferences.userRoot().node("com.clusterview.auth")
    private const val KEY_STAY_LOGGED_IN = "stay_logged_in"
    private const val KEY_USER_ID = "logged_in_user_id"

    fun isUserRemembered(): Boolean = prefs.getBoolean(KEY_STAY_LOGGED_IN, false)

    fun getSavedUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun login(email: String, pass: String, remember: Boolean): User? {
        if (email.isBlank() || pass.isBlank()) return null

        val user = DatabaseManager.verifyLogin(email, pass)

        if (user != null) {
            if (remember) {
                prefs.putBoolean(KEY_STAY_LOGGED_IN, true)
                prefs.putInt(KEY_USER_ID, user.id)
            } else {
                clearPersistence()
            }
            return user
        }

        return null
    }

    fun logout() {
        clearPersistence()
    }

    private fun clearPersistence() {
        prefs.remove(KEY_STAY_LOGGED_IN)
        prefs.remove(KEY_USER_ID)
        prefs.flush()
    }
}
