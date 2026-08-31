package com.shubhang.loophole.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.shubhang.loophole.appContainer
import com.shubhang.loophole.settings.SecureSetting

/** Pushes updated values into every placed widget and re-renders it. */
class WidgetUpdater(context: Context) {

    private val appContext = context.applicationContext

    suspend fun refresh() {
        val devSettings = appContext.appContainer.devSettings
        val devEnabled = devSettings.currentValue(SecureSetting.DEV_OPTIONS)
        val usbEnabled = devSettings.currentValue(SecureSetting.USB_DEBUGGING)
        val wirelessEnabled = devSettings.currentValue(SecureSetting.WIRELESS_DEBUGGING)

        val widget = LoopholeWidget()
        val ids = GlanceAppWidgetManager(appContext).getGlanceIds(LoopholeWidget::class.java)
        ids.forEach { id ->
            updateAppWidgetState(appContext, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[DevModeEnabledKey] = devEnabled
                    this[UsbDebugEnabledKey] = usbEnabled
                    this[WirelessDebugEnabledKey] = wirelessEnabled
                }
            }
            widget.update(appContext, id)
        }
    }
}
