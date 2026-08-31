package com.shubhang.loophole.settings

import android.os.Build
import android.provider.Settings

/**
 * A boolean flag in [Settings.Global] that the app can read and write.
 *
 * Reading needs no permission; writing requires the signature-level
 * WRITE_SECURE_SETTINGS, granted once over adb:
 *
 *   adb shell pm grant com.shubhang.loophole android.permission.WRITE_SECURE_SETTINGS
 */
enum class SecureSetting(val key: String) {
    DEV_OPTIONS(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED),

    /** "USB debugging" in Developer Options. */
    USB_DEBUGGING(Settings.Global.ADB_ENABLED),

    /**
     * "Wireless debugging" in Developer Options. Added in Android 11; the
     * platform constant is @hide, so the key is spelled out. Writing it on
     * older releases just stores a value nothing reads.
     */
    WIRELESS_DEBUGGING("adb_wifi_enabled"),
    ;

    val isSupportedOnCurrentSdk: Boolean
        get() = when (this) {
            WIRELESS_DEBUGGING -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            else -> true
        }

    companion object {
        /** Cleared before Developer Options is switched on. */
        val DEBUGGING = listOf(USB_DEBUGGING, WIRELESS_DEBUGGING)
    }
}
