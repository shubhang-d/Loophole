package com.shubhang.loophole.ui

import android.app.Activity
import android.os.Bundle
import com.shubhang.loophole.appContainer

/**
 * Invisible trampoline. The home-screen widget's gear button launches this
 * (an explicit, same-app target, which works reliably from a widget click),
 * and it immediately forwards to the system Developer Options screen, then
 * finishes. This avoids launching an implicit intent straight from the widget
 * PendingIntent, which the platform blocks on Android 14+.
 */
class DevOptionsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer.developerOptionsLauncher.open()
        finish()
    }
}
