package com.clusterview.demo

import java.util.prefs.Preferences

/*object AuthManager {
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
}*/
object AuthManager {
    // This creates a small hidden file on your OS to store the login ID
    private val prefs = Preferences.userNodeForPackage(AuthManager::class.java)
    private const val ID_KEY = "operator_id"

    // 1. Save the ID to disk
    fun saveUser(id: Int) {
        prefs.putInt(ID_KEY, id)
        prefs.flush() // Force the OS to write it NOW
    }

    // 2. Retrieve the ID from disk
    fun getSavedUserId(): Int = prefs.getInt(ID_KEY, -1)

    // 3. Check if we have a valid saved ID
    fun isUserRemembered(): Boolean = getSavedUserId() != -1

    // 4. Log out (Clear the file)
    fun clear() {
        prefs.remove(ID_KEY)
        prefs.flush()
    }
}
