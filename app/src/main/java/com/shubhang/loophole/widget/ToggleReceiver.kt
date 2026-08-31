package com.shubhang.loophole.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shubhang.loophole.settings.SecureSetting

/**
 * Handles toggling settings in the background.
 * Triggered by the widget via a Broadcast intent.
 */
class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("Loophole", "ToggleReceiver received intent: $action")

        val settingName = intent.getStringExtra(EXTRA_SETTING) ?: when (action) {
            ACTION_TOGGLE_USB_DEBUG -> SecureSetting.USB_DEBUGGING.name
            ACTION_TOGGLE_WIRELESS_DEBUG -> SecureSetting.WIRELESS_DEBUGGING.name
            else -> SecureSetting.DEV_OPTIONS.name
        }

        ToggleWorker.enqueue(context, settingName)
    }

    companion object {
        const val ACTION_TOGGLE_DEV_MODE = "com.shubhang.loophole.action.TOGGLE_DEV_MODE"
        const val ACTION_TOGGLE_USB_DEBUG = "com.shubhang.loophole.action.TOGGLE_USB_DEBUG"
        const val ACTION_TOGGLE_WIRELESS_DEBUG = "com.shubhang.loophole.action.TOGGLE_WIRELESS_DEBUG"
        const val EXTRA_SETTING = "com.shubhang.loophole.extra.SETTING"

        fun createToggleIntent(context: Context, setting: SecureSetting): Intent =
            Intent(context, ToggleReceiver::class.java).apply {
                action = when (setting) {
                    SecureSetting.USB_DEBUGGING -> ACTION_TOGGLE_USB_DEBUG
                    SecureSetting.WIRELESS_DEBUGGING -> ACTION_TOGGLE_WIRELESS_DEBUG
                    SecureSetting.DEV_OPTIONS -> ACTION_TOGGLE_DEV_MODE
                }
                putExtra(EXTRA_SETTING, setting.name)
            }
    }
}
