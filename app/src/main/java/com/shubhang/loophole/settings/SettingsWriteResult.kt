package com.shubhang.loophole.settings

/**
 * Outcome of a write. Missing permission is an expected result rather than an
 * exceptional one, because WRITE_SECURE_SETTINGS cannot be requested at runtime.
 */
sealed interface SettingsWriteResult {

    /** Written; [isEnabled] is the value read back afterwards. */
    data class Success(val isEnabled: Boolean) : SettingsWriteResult

    /** WRITE_SECURE_SETTINGS has not been granted over adb. */
    data object PermissionDenied : SettingsWriteResult
}
