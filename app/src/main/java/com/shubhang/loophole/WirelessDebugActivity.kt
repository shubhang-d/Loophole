package com.shubhang.loophole

import android.app.Activity
import android.os.Bundle

/**
 * Invisible trampoline. The home-screen widget's button launches this
 * and it immediately forwards to the system Wireless Debugging options screen,
 * then finishes.
 */
class WirelessDebugActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WirelessDevMode.openWirelessDebugging(this)
        finish()
    }
}
