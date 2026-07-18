package com.shubhang.loophole.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.shubhang.loophole.DevMode
import com.shubhang.loophole.DevOptionsActivity
import com.shubhang.loophole.R

val EnabledKey = booleanPreferencesKey("dev_mode_enabled")

class LoopholeWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Fall back to the live setting the first time, before any write has
            // seeded the store (e.g. right after the widget is placed).
            val enabled = currentState(EnabledKey) ?: DevMode.isEnabled(context)
            GlanceTheme {
                WidgetBody(enabled)
            }
        }
    }
}

@Composable
private fun WidgetBody(enabled: Boolean) {
    val chipBackground = if (enabled) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
    val chipForeground = if (enabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant
    val gearBackground = if (enabled) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surface
    val gearForeground = if (enabled) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurfaceVariant

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(chipBackground)
            .cornerRadius(28.dp)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading: status dot + ON/OFF label. Fills the remaining width so the
        // whole left region is the toggle target and the label sits flush-left.
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .clickable(actionSendBroadcast<ToggleReceiver>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(12.dp)
                    .cornerRadius(6.dp)
                    .background(chipForeground)
            ) {}
            Spacer(GlanceModifier.width(12.dp))
            Text(
                text = if (enabled) "ON" else "OFF",
                style = TextStyle(
                    color = chipForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            )
        }

        Spacer(GlanceModifier.width(12.dp))

        Box(
            modifier = GlanceModifier
                .size(44.dp)
                .cornerRadius(22.dp)
                .background(gearBackground)
                .clickable(actionStartActivity<DevOptionsActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_settings_gear),
                contentDescription = "Open Developer Options",
                colorFilter = ColorFilter.tint(gearForeground),
                modifier = GlanceModifier.size(22.dp)
            )
        }
    }
}

class LoopholeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoopholeWidget()
}

suspend fun refreshLoopholeWidgets(context: Context) {
    val enabled = DevMode.isEnabled(context)
    val widget = LoopholeWidget()
    val ids = GlanceAppWidgetManager(context).getGlanceIds(LoopholeWidget::class.java)
    ids.forEach { id ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply { this[EnabledKey] = enabled }
        }
        widget.update(context, id)
    }
}