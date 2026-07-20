package com.shubhang.loophole.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shubhang.loophole.ToggleTarget

/**
 * Handles the Dev Mode, USB Debugging, and Wireless Debugging toggles in the background.
 * Triggered by the widget via Broadcast intents.
 */
class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Loophole", "ToggleReceiver received intent: ${intent.action}")
        val targetName = intent.getStringExtra(EXTRA_TARGET)
        val target = when {
            targetName != null -> try {
                ToggleTarget.valueOf(targetName)
            } catch (e: Exception) {
                ToggleTarget.DEV_MODE
            }
            intent.action == ACTION_TOGGLE_USB_DEBUG -> ToggleTarget.USB_DEBUG
            intent.action == ACTION_TOGGLE_WIRELESS_DEBUG -> ToggleTarget.WIRELESS_DEBUG
            else -> ToggleTarget.DEV_MODE
        }
        ToggleWorker.enqueue(context, target)
    }

    companion object {
        const val EXTRA_TARGET = "com.shubhang.loophole.extra.TARGET"
        const val ACTION_TOGGLE_DEV_MODE = "com.shubhang.loophole.action.TOGGLE_DEV_MODE"
        const val ACTION_TOGGLE_USB_DEBUG = "com.shubhang.loophole.action.TOGGLE_USB_DEBUG"
        const val ACTION_TOGGLE_WIRELESS_DEBUG = "com.shubhang.loophole.action.TOGGLE_WIRELESS_DEBUG"
    }
}
