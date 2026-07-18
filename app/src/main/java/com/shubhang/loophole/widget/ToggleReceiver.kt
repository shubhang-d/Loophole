package com.shubhang.loophole.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Handles the Dev Mode toggle in the background.
 * Triggered by the widget via a Broadcast intent.
 */
class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Loophole", "ToggleReceiver received intent: ${intent.action}")
        if (intent.action != null && intent.action != ACTION_TOGGLE_DEV_MODE) return
        ToggleWorker.enqueue(context)
    }

    companion object {
        const val ACTION_TOGGLE_DEV_MODE = "com.shubhang.loophole.action.TOGGLE_DEV_MODE"
    }
}
