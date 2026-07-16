package com.shubhang.loophole

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Single source of truth for reading, toggling, and navigating to Android's
 * Developer Options. Shared by [MainActivity], [DevModeTileService], and the
 * home-screen widget so the behaviour stays identical everywhere.
 *
 * Reading [Settings.Global.DEVELOPMENT_SETTINGS_ENABLED] needs no permission.
 * Writing it requires the signature-level WRITE_SECURE_SETTINGS permission,
 * granted once over adb:
 *
 *   adb shell pm grant com.shubhang.loophole android.permission.WRITE_SECURE_SETTINGS
 */
object DevMode {

    private const val TAG = "Loophole"

    fun isEnabled(context: Context): Boolean =
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1

    /**
     * Writes the new state.
     * @return true on success, false if WRITE_SECURE_SETTINGS has not been granted.
     */
    fun setEnabled(context: Context, enabled: Boolean): Boolean = try {
        Settings.Global.putInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            if (enabled) 1 else 0
        )
        true
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
