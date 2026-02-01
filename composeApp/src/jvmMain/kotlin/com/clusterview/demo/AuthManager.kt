package com.clusterview.demo

import java.util.prefs.Preferences

/*object AuthManager {
    private val prefs = java.util.prefs.Preferences.userRoot().node("com.clusterview.auth")
    private const val KEY_STAY_LOGGED_IN = "stay_logged_in"
    private const val KEY_USER_ID = "logged_in_user_id"

    // Check if we should skip the login screen
    //fun isUserRemembered(): Boolean = prefs.getBoolean(KEY_STAY_LOGGED_IN, false)

    //fun getSavedUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    // In AuthManager.kt
    fun login(email: String, pass: String, remember: Boolean): User? {
        // If login is successful, return a User object
        return if (email == "test" && pass == "test") {
            User(id = 1, email = email, username = "Admin")
        } else {
            null // Return null if it fails
        }
    }


        fun login(email: String, pass: String, remember: Boolean): User? {
            // 1. Basic validation (Ensure fields aren't empty)
            if (email.isBlank() || pass.isBlank()) return null

            // 2. Database Lookup (Simulated for now)
            // In a real app, you'd use: val user = db.getUserByEmail(email)
            val mockUser = User(
                id = 1,
                email = "admin@cluster.com",
                username = "Admin_Operator",
                passwordHash = "hashed_pass_123"
            )
            return if (email == "admin" && pass == "password") {
                User(
                    id = 1,
                    email = email,
                    username = "AdminUser",
                    passwordHash = "---" // ADD THIS LINE
                )
            } else {
                null
            }
        }


    fun isUserRemembered(): Boolean = false
    fun getSavedUserId(): Int = -1
    fun logout() {
        prefs.remove(KEY_STAY_LOGGED_IN)
        prefs.remove(KEY_USER_ID)
    }
}*/

object AuthManager {
    private val prefs = Preferences.userRoot().node("com.clusterview.auth")
    private const val KEY_STAY_LOGGED_IN = "stay_logged_in"
    private const val KEY_USER_ID = "logged_in_user_id"

    // Check if we should skip the login screen
    fun isUserRemembered(): Boolean = prefs.getBoolean(KEY_STAY_LOGGED_IN, false)

    fun getSavedUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    /**
     * Authenticates the user and handles the "Stay Logged In" preference.
     */
    fun login(email: String, pass: String, remember: Boolean): User? {
        // 1. Basic validation
        if (email.isBlank() || pass.isBlank()) return null

        // 2. Check the "Database" (Our list in DatabaseManager)
        val user = DatabaseManager.verifyLogin(email, pass)

        if (user != null) {
            // 3. Handle the "Stay Logged In" persistence
            if (remember) {
                prefs.putBoolean(KEY_STAY_LOGGED_IN, true)
                prefs.putInt(KEY_USER_ID, user.id)
            } else {
                // If they didn't check the box, make sure we clear any old saved data
                clearPersistence()
            }
            return user
        }

        return null // Login failed
    }

    fun logout() {
        clearPersistence()
    }

    private fun clearPersistence() {
        prefs.remove(KEY_STAY_LOGGED_IN)
        prefs.remove(KEY_USER_ID)
        prefs.flush() // Ensure changes are saved to disk
    }
}

/*
data class User(
    val id: Int,
    val email: String,
    val username: String,
    val passwordHash: String = "" // Never store plain text passwords!
)*/
