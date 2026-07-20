package com.shubhang.loophole.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Handles the Wireless Debugging toggle in the background.
 * Triggered by the widget via a Broadcast intent.
 */
class WirelessToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Loophole", "WirelessToggleReceiver received intent: ${intent.action}")
        if (intent.action != null && intent.action != ACTION_TOGGLE_WIRELESS_DEBUG) return
        WirelessToggleWorker.enqueue(context)
    }

    companion object {
        const val ACTION_TOGGLE_WIRELESS_DEBUG = "com.shubhang.loophole.action.TOGGLE_WIRELESS_DEBUG"
    }
}
