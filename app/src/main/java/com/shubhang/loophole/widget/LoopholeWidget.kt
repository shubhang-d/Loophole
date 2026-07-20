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
import com.shubhang.loophole.ToggleTarget
import com.shubhang.loophole.UsbDebug
import com.shubhang.loophole.WirelessDebug

val EnabledKey = booleanPreferencesKey("dev_mode_enabled")

/** Dedicated home-screen widget for toggling Developer Options (`DEV`). */
class LoopholeWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val enabled = currentState(EnabledKey) ?: DevMode.isEnabled(context)
            GlanceTheme {
                DevWidgetBody(enabled = enabled, context = context)
            }
        }
    }
}

@Composable
private fun DevWidgetBody(enabled: Boolean, context: Context) {
    val devIntent = Intent(context, ToggleReceiver::class.java).apply {
        putExtra(ToggleReceiver.EXTRA_TARGET, ToggleTarget.DEV_MODE.name)
    }

    val chipBg = if (enabled) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
    val chipFg = if (enabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant
    val gearBg = if (enabled) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surface
    val gearFg = if (enabled) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurfaceVariant

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
                .clickable(actionSendBroadcast(devIntent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_dev_mode_tile),
                contentDescription = null,
                colorFilter = ColorFilter.tint(chipFg),
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(GlanceModifier.width(10.dp))
            Text(
                text = if (enabled) "DEV ON" else "DEV OFF",
                style = TextStyle(
                    color = chipFg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
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

class LoopholeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoopholeWidget()
}

suspend fun refreshLoopholeWidgets(context: Context) {
    val devEnabled = DevMode.isEnabled(context)
    val usbEnabled = UsbDebug.isEnabled(context)
    val wirelessEnabled = WirelessDebug.isEnabled(context)

    // 1. Refresh Dev Mode Widget
    val devWidget = LoopholeWidget()
    val devIds = GlanceAppWidgetManager(context).getGlanceIds(LoopholeWidget::class.java)
    devIds.forEach { id ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply { this[EnabledKey] = devEnabled }
        }
        devWidget.update(context, id)
    }

    // 2. Refresh USB Debugging Widget
    val usbWidget = UsbDebugWidget()
    val usbIds = GlanceAppWidgetManager(context).getGlanceIds(UsbDebugWidget::class.java)
    usbIds.forEach { id ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply { this[UsbEnabledKey] = usbEnabled }
        }
        usbWidget.update(context, id)
    }

    // 3. Refresh Wireless Debugging Widget
    val wirelessWidget = WirelessDebugWidget()
    val wirelessIds = GlanceAppWidgetManager(context).getGlanceIds(WirelessDebugWidget::class.java)
    wirelessIds.forEach { id ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply { this[WirelessEnabledKey] = wirelessEnabled }
        }
        wirelessWidget.update(context, id)
    }

    // 4. Refresh All-in-One Multi Widget
    val comboWidget = LoopholeComboWidget()
    val comboIds = GlanceAppWidgetManager(context).getGlanceIds(LoopholeComboWidget::class.java)
    comboIds.forEach { id ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                this[ComboDevKey] = devEnabled
                this[ComboUsbKey] = usbEnabled
                this[ComboWirelessKey] = wirelessEnabled
            }
        }
        comboWidget.update(context, id)
    }
}