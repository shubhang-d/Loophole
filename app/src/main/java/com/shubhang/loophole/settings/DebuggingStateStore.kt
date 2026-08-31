package com.shubhang.loophole.settings

import android.content.SharedPreferences

/**
 * Persists the state of secondary debugging flags (e.g. USB debugging, Wireless debugging)
 * when Developer Options is temporarily turned off, so that re-enabling Developer Options
 * restores the user's previous configuration.
 */
interface DebuggingStateStore {
    fun getSavedState(setting: SecureSetting): Boolean
    fun saveState(setting: SecureSetting, enabled: Boolean)
    fun clear()
}

/**
 * In-memory implementation used for unit tests and fallback.
 */
class InMemoryDebuggingStateStore : DebuggingStateStore {
    private val memory = mutableMapOf<SecureSetting, Boolean>()

    override fun getSavedState(setting: SecureSetting): Boolean =
        memory[setting] ?: false

    override fun saveState(setting: SecureSetting, enabled: Boolean) {
        memory[setting] = enabled
    }

    override fun clear() {
        memory.clear()
    }
}

/**
 * SharedPreferences-backed implementation for persistent storage across app and widget lifecycles.
 */
class SharedPreferencesDebuggingStateStore(
    private val preferences: SharedPreferences
) : DebuggingStateStore {

    override fun getSavedState(setting: SecureSetting): Boolean =
        preferences.getBoolean(keyFor(setting), false)

    override fun saveState(setting: SecureSetting, enabled: Boolean) {
        preferences.edit().putBoolean(keyFor(setting), enabled).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private fun keyFor(setting: SecureSetting): String = "saved_state_${setting.name}"
}
