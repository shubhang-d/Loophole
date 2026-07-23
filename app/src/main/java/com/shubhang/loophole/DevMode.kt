package com.shubhang.loophole

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

enum class ToggleTarget {
    DEV_MODE,
    USB_DEBUG,
    WIRELESS_DEBUG
}

/**
 * Single source of truth for reading, toggling, and navigating to Android's
 * Developer Options. Shared by [MainActivity], [DevModeTileService], and the
 * home-screen widget so the behaviour stays identical everywhere.
 */
object DevMode {

    private const val TAG = "Loophole"

    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1
        Log.d(TAG, "DevMode isEnabled: $enabled")
        return enabled
    }

    /**
     * Writes the new state.
     * @return true on success, false if WRITE_SECURE_SETTINGS has not been granted.
     */
    fun setEnabled(context: Context, enabled: Boolean): Boolean = try {
        Log.d(TAG, "DevMode setEnabled: setting to $enabled")
        val success = Settings.Global.putInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            if (enabled) 1 else 0
        )
        Log.d(TAG, "DevMode setEnabled: putInt returned $success, final value read: ${isEnabled(context)}")
        success
    } catch (e: SecurityException) {
        Log.e(
            TAG,
            "Cannot write DEVELOPMENT_SETTINGS_ENABLED — WRITE_SECURE_SETTINGS not granted. " +
                "Run: adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS",
            e
        )
        false
    }

    /** Flips the current state. @return true on success, false if the permission is missing. */
    fun toggle(context: Context): Boolean = setEnabled(context, !isEnabled(context))

    /** Opens the system Developer Options screen (falls back to top-level Settings). */
    fun openDeveloperOptions(context: Context) {
        val devOptions = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(devOptions)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** Intent that opens Developer Options, for use with widget/tile PendingIntents. */
    fun developerOptionsIntent(): Intent =
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

/** Single source of truth for USB Debugging (`ADB_ENABLED`). */
object UsbDebug {

    private const val TAG = "Loophole"

    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) == 1
        Log.d(TAG, "UsbDebug isEnabled: $enabled")
        return enabled
    }

    fun setEnabled(context: Context, enabled: Boolean): Boolean = try {
        Log.d(TAG, "UsbDebug setEnabled: setting to $enabled")
        val success = Settings.Global.putInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            if (enabled) 1 else 0
        )
        Log.d(TAG, "UsbDebug setEnabled: putInt returned $success, final value read: ${isEnabled(context)}")
        success
    } catch (e: SecurityException) {
        Log.e(
            TAG,
            "Cannot write ADB_ENABLED — WRITE_SECURE_SETTINGS not granted.",
            e
        )
        false
    }

    fun toggle(context: Context): Boolean = setEnabled(context, !isEnabled(context))
}

/** Single source of truth for Wireless Debugging (`adb_wifi_enabled`). Available on Android 11+ (API 30+). */
object WirelessDebug {

    private const val TAG = "Loophole"
    const val SETTING_KEY = "adb_wifi_enabled"

    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun isEnabled(context: Context): Boolean {
        if (!isSupported()) return false
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            SETTING_KEY,
            0
        ) == 1
        Log.d(TAG, "WirelessDebug isEnabled: $enabled")
        return enabled
    }

    fun setEnabled(context: Context, enabled: Boolean): Boolean {
        if (!isSupported()) return false
        return try {
            Log.d(TAG, "WirelessDebug setEnabled: setting to $enabled")
            val success = Settings.Global.putInt(
                context.contentResolver,
                SETTING_KEY,
                if (enabled) 1 else 0
            )
            Log.d(TAG, "WirelessDebug setEnabled: putInt returned $success, final value read: ${isEnabled(context)}")
            success
        } catch (e: SecurityException) {
            Log.e(
                TAG,
                "Cannot write adb_wifi_enabled — WRITE_SECURE_SETTINGS not granted.",
                e
            )
            false
        }
    }

    fun toggle(context: Context): Boolean = setEnabled(context, !isEnabled(context))
}
