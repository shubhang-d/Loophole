package com.shubhang.loophole.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings

/** Opens the system Developer Options screen, falling back to top-level Settings. */
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
}
