package com.shubhang.loophole.widget

import android.content.Context
import android.content.Intent
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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
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
import com.shubhang.loophole.DevOptionsActivity
import com.shubhang.loophole.R
import com.shubhang.loophole.ToggleTarget
import com.shubhang.loophole.WirelessDebug

val WirelessEnabledKey = booleanPreferencesKey("wireless_debug_enabled")

/** Dedicated home-screen widget for toggling Wireless Debugging (`WIFI`). */
class WirelessDebugWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val enabled = currentState(WirelessEnabledKey) ?: WirelessDebug.isEnabled(context)
            val supported = WirelessDebug.isSupported()
            GlanceTheme {
                WirelessWidgetBody(enabled = enabled, supported = supported, context = context)
            }
        }
    }
}

@Composable
private fun WirelessWidgetBody(enabled: Boolean, supported: Boolean, context: Context) {
    val wirelessIntent = Intent(context, ToggleReceiver::class.java).apply {
        putExtra(ToggleReceiver.EXTRA_TARGET, ToggleTarget.WIRELESS_DEBUG.name)
    }

    val chipBg = if (enabled && supported) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
    val chipFg = if (enabled && supported) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant
    val gearBg = if (enabled && supported) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surface
    val gearFg = if (enabled && supported) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurfaceVariant

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(chipBg)
            .cornerRadius(28.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .clickable(actionSendBroadcast(wirelessIntent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_wireless_tile),
                contentDescription = null,
                colorFilter = ColorFilter.tint(chipFg),
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(GlanceModifier.width(10.dp))
            Text(
                text = if (!supported) "N/A (11+)" else if (enabled) "WIFI ON" else "WIFI OFF",
                style = TextStyle(
                    color = chipFg,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (supported) 20.sp else 16.sp
                )
            )
        }

        Spacer(GlanceModifier.width(8.dp))

        Box(
            modifier = GlanceModifier
                .size(42.dp)
                .cornerRadius(21.dp)
                .background(gearBg)
                .clickable(actionStartActivity<DevOptionsActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_settings_gear),
                contentDescription = "Open Developer Options",
                colorFilter = ColorFilter.tint(gearFg),
                modifier = GlanceModifier.size(20.dp)
            )
        }
    }
}

class WirelessDebugWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WirelessDebugWidget()
}
