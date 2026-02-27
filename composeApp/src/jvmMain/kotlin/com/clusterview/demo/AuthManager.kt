package com.clusterview.demo

import java.util.prefs.Preferences

object AuthManager {
    private val prefs = Preferences.userNodeForPackage(AuthManager::class.java)
    private const val ID_KEY = "operator_id"

    fun saveUser(id: Int) {
        prefs.putInt(ID_KEY, id)
        prefs.flush()
    }

    fun getSavedUserId(): Int = prefs.getInt(ID_KEY, -1)

    fun isUserRemembered(): Boolean = getSavedUserId() != -1

    fun clear() {
        prefs.remove(ID_KEY)
        prefs.flush()
    }
}
