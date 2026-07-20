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
import com.shubhang.loophole.WirelessDevMode
import com.shubhang.loophole.WirelessDebugActivity
import com.shubhang.loophole.R

val WirelessEnabledKey = booleanPreferencesKey("wireless_debug_enabled")

class WirelessDebugWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val enabled = currentState(WirelessEnabledKey) ?: WirelessDevMode.isEnabled(context)
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
            .cornerRadius(20.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .clickable(actionSendBroadcast<WirelessToggleReceiver>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .cornerRadius(4.dp)
                    .background(chipForeground)
            ) {}
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = if (enabled) "ON" else "OFF",
                style = TextStyle(
                    color = chipForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        }

        Spacer(GlanceModifier.width(8.dp))

        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(18.dp)
                .background(gearBackground)
                .clickable(actionStartActivity<WirelessDebugActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_wireless_debug),
                contentDescription = "Open Wireless Debugging",
                colorFilter = ColorFilter.tint(gearForeground),
                modifier = GlanceModifier.size(18.dp)
            )
        }
    }
}

class WirelessDebugWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WirelessDebugWidget()
}

suspend fun refreshWirelessDebugWidgets(context: Context) {
    val enabled = WirelessDevMode.isEnabled(context)
    val widget = WirelessDebugWidget()
    val ids = GlanceAppWidgetManager(context).getGlanceIds(WirelessDebugWidget::class.java)
    ids.forEach { id ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply { this[WirelessEnabledKey] = enabled }
        }
        widget.update(context, id)
    }
}
