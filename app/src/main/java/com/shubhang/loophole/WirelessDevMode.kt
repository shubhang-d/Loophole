package com.shubhang.loophole

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

import android.content.ComponentName

/**
 * Single source of truth for reading, toggling, and navigating to Android's
 * Wireless Debugging settings.
 *
 * Reading Settings.Global.ADB_WIFI_ENABLED ("adb_wifi_enabled") needs no permission.
 * Writing it requires WRITE_SECURE_SETTINGS permission.
 */
object WirelessDevMode {

    private const val TAG = "Loophole"
    private const val ADB_WIFI_ENABLED = "adb_wifi_enabled"

    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            ADB_WIFI_ENABLED,
            0
        ) == 1
        Log.d(TAG, "wirelessIsEnabled: $enabled")
        return enabled
    }

    fun setEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            Log.d(TAG, "wirelessSetEnabled: setting to $enabled")
            if (enabled && !DevMode.isEnabled(context)) {
                if (!DevMode.setEnabled(context, true)) {
                    return false
                }
            }
            val success = Settings.Global.putInt(
                context.contentResolver,
                ADB_WIFI_ENABLED,
                if (enabled) 1 else 0
            )
            Log.d(TAG, "wirelessSetEnabled: putInt returned $success, final value read: ${isEnabled(context)}")
            success
        } catch (e: SecurityException) {
            Log.e(
                TAG,
                "Cannot write ADB_WIFI_ENABLED — WRITE_SECURE_SETTINGS not granted.",
                e
            )
            false
        }
    }

    /** Flips the current state. @return true on success, false if the permission is missing. */
    fun toggle(context: Context): Boolean = setEnabled(context, !isEnabled(context))

    /** Opens the system Wireless Debugging settings screen (falls back to Developer Options). */
    fun openWirelessDebugging(context: Context) {
        // Direct shortcuts to Wireless Debugging settings are aggressively intercepted
        // by modern OS frameworks (like Oppo/OnePlus/Realme's OplusAppListInterceptManager)
        // and redirected to the Safety Center (Security & Privacy).
        // Opening Developer Options directly is the most reliable way to let the user access Wireless Debugging.
        DevMode.openDeveloperOptions(context)
    }

    /** Intent that opens Developer Options, for use with widget/tile PendingIntents. */
    fun wirelessDebuggingIntent(): Intent =
        DevMode.developerOptionsIntent()
}
