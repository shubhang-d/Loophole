package com.shubhang.loophole.settings

import kotlinx.coroutines.flow.Flow

/**
 * The boundary against the Android framework's settings store. This is the one
 * interface in the app: faking it is what lets the repository and ViewModel be
 * tested without a device.
 */
interface SecureSettingsSource {

    fun read(setting: SecureSetting): Boolean

    /** @return true on success, false if WRITE_SECURE_SETTINGS is not granted. */
    fun write(setting: SecureSetting, enabled: Boolean): Boolean

    /** Emits the current value, then re-emits whenever the system value changes. */
    fun observe(setting: SecureSetting): Flow<Boolean>
}
