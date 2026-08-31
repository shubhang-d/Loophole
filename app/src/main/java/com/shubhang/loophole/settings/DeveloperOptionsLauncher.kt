package com.shubhang.loophole.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/** Opens the system Developer Options or Wireless Debugging screen, falling back gracefully. */
class DeveloperOptionsLauncher(context: Context) {

    private val appContext = context.applicationContext

    fun open() {
        val devOptions = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            appContext.startActivity(devOptions)
        } catch (e: ActivityNotFoundException) {
            appContext.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openWirelessDebugging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intents = listOf(
                Intent(ACTION_WIRELESS_DEBUGGING_SETTINGS),
                Intent().setClassName("com.android.settings", "com.android.settings.SubSettings")
                    .putExtra(EXTRA_SHOW_FRAGMENT, WIRELESS_DEBUGGING_FRAGMENT),
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    .putExtra(EXTRA_SHOW_FRAGMENT, WIRELESS_DEBUGGING_FRAGMENT),
            )
            for (intent in intents) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    appContext.startActivity(intent)
                    return
                } catch (_: Exception) {}
            }
        }
        open()
    }

    companion object {
        private const val ACTION_WIRELESS_DEBUGGING_SETTINGS = "android.settings.WIRELESS_DEBUGGING_SETTINGS"
        private const val EXTRA_SHOW_FRAGMENT = ":settings:show_fragment"
        private const val WIRELESS_DEBUGGING_FRAGMENT = "com.android.settings.development.WirelessDebuggingFragment"
    }
}
