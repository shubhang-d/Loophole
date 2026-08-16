package com.shubhang.loophole.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

/** Pushes a new value into every placed widget and re-renders it. */
class WidgetUpdater(context: Context) {

    private val appContext = context.applicationContext

    suspend fun refresh(isEnabled: Boolean) {
        val widget = LoopholeWidget()
        val ids = GlanceAppWidgetManager(appContext).getGlanceIds(LoopholeWidget::class.java)
        ids.forEach { id ->
            updateAppWidgetState(appContext, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply { this[EnabledKey] = isEnabled }
            }
            widget.update(appContext, id)
        }
    }
}
