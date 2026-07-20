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
import androidx.glance.layout.Column
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

val ComboDevKey = booleanPreferencesKey("combo_dev_enabled")
val ComboUsbKey = booleanPreferencesKey("combo_usb_enabled")
val ComboWirelessKey = booleanPreferencesKey("combo_wireless_enabled")

/** All-in-One Icon-based Multi Widget containing DEV, USB, and WIFI icons + Gear icon. */
class LoopholeComboWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val devEnabled = currentState(ComboDevKey) ?: DevMode.isEnabled(context)
            val usbEnabled = currentState(ComboUsbKey) ?: UsbDebug.isEnabled(context)
            val wirelessEnabled = currentState(ComboWirelessKey) ?: WirelessDebug.isEnabled(context)
            val wirelessSupported = WirelessDebug.isSupported()

            GlanceTheme {
                ComboWidgetBody(
                    devEnabled = devEnabled,
                    usbEnabled = usbEnabled,
                    wirelessEnabled = wirelessEnabled,
                    wirelessSupported = wirelessSupported,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun ComboWidgetBody(
    devEnabled: Boolean,
    usbEnabled: Boolean,
    wirelessEnabled: Boolean,
    wirelessSupported: Boolean,
    context: Context
) {
    val devIntent = Intent(context, ToggleReceiver::class.java).apply {
        putExtra(ToggleReceiver.EXTRA_TARGET, ToggleTarget.DEV_MODE.name)
    }
    val usbIntent = Intent(context, ToggleReceiver::class.java).apply {
        putExtra(ToggleReceiver.EXTRA_TARGET, ToggleTarget.USB_DEBUG.name)
    }
    val wirelessIntent = Intent(context, ToggleReceiver::class.java).apply {
        putExtra(ToggleReceiver.EXTRA_TARGET, ToggleTarget.WIRELESS_DEBUG.name)
    }

    val containerBg = GlanceTheme.colors.surface
    val chipDevBg = if (devEnabled) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
    val chipDevFg = if (devEnabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant

    val chipUsbBg = if (usbEnabled) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
    val chipUsbFg = if (usbEnabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant

    val chipWirelessBg = if (wirelessEnabled && wirelessSupported) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
    val chipWirelessFg = if (wirelessEnabled && wirelessSupported) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(containerBg)
            .cornerRadius(24.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chip 1: Dev Mode (Icon + Status)
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .background(chipDevBg)
                .cornerRadius(18.dp)
                .clickable(actionSendBroadcast(devIntent))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    provider = ImageProvider(R.drawable.ic_dev_mode_tile),
                    contentDescription = "Dev Mode",
                    colorFilter = ColorFilter.tint(chipDevFg),
                    modifier = GlanceModifier.size(20.dp)
                )
                Text(
                    text = if (devEnabled) "ON" else "OFF",
                    style = TextStyle(
                        color = chipDevFg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(GlanceModifier.width(6.dp))

        // Chip 2: USB Debugging (Icon + Status)
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .background(chipUsbBg)
                .cornerRadius(18.dp)
                .clickable(actionSendBroadcast(usbIntent))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    provider = ImageProvider(R.drawable.ic_usb_tile),
                    contentDescription = "USB Debugging",
                    colorFilter = ColorFilter.tint(chipUsbFg),
                    modifier = GlanceModifier.size(20.dp)
                )
                Text(
                    text = if (usbEnabled) "ON" else "OFF",
                    style = TextStyle(
                        color = chipUsbFg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(GlanceModifier.width(6.dp))

        // Chip 3: Wireless Debugging (Icon + Status)
        if (wirelessSupported) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .background(chipWirelessBg)
                    .cornerRadius(18.dp)
                    .clickable(actionSendBroadcast(wirelessIntent))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_wireless_tile),
                        contentDescription = "Wireless Debugging",
                        colorFilter = ColorFilter.tint(chipWirelessFg),
                        modifier = GlanceModifier.size(20.dp)
                    )
                    Text(
                        text = if (wirelessEnabled) "ON" else "OFF",
                        style = TextStyle(
                            color = chipWirelessFg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(GlanceModifier.width(6.dp))
        }

        // Gear Icon button
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(20.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .clickable(actionStartActivity<DevOptionsActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_settings_gear),
                contentDescription = "Open Developer Options",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                modifier = GlanceModifier.size(20.dp)
            )
        }
    }
}

class LoopholeComboWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoopholeComboWidget()
}
