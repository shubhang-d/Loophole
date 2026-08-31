package com.shubhang.loophole.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
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
import com.shubhang.loophole.R
import com.shubhang.loophole.appContainer
import com.shubhang.loophole.settings.SecureSetting
import com.shubhang.loophole.ui.DevOptionsActivity

val DevModeEnabledKey = booleanPreferencesKey("dev_mode_enabled")
val UsbDebugEnabledKey = booleanPreferencesKey("usb_debugging_enabled")
val WirelessDebugEnabledKey = booleanPreferencesKey("wireless_debugging_enabled")

class LoopholeWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SIZE,
            MEDIUM_SIZE,
            LARGE_SIZE,
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val devSettings = context.appContainer.devSettings
        val liveDev = devSettings.currentValue(SecureSetting.DEV_OPTIONS)
        val liveUsb = devSettings.currentValue(SecureSetting.USB_DEBUGGING)
        val liveWireless = devSettings.currentValue(SecureSetting.WIRELESS_DEBUGGING)

        provideContent {
            val devEnabled = currentState(DevModeEnabledKey) ?: liveDev
            val usbEnabled = currentState(UsbDebugEnabledKey) ?: liveUsb
            val wirelessEnabled = currentState(WirelessDebugEnabledKey) ?: liveWireless

            GlanceTheme {
                AdaptiveWidgetBody(
                    devEnabled = devEnabled,
                    usbEnabled = usbEnabled,
                    wirelessEnabled = wirelessEnabled,
                )
            }
        }
    }

    companion object {
        val SMALL_SIZE = DpSize(120.dp, 48.dp)
        val MEDIUM_SIZE = DpSize(200.dp, 48.dp)
        val LARGE_SIZE = DpSize(280.dp, 48.dp)

        /** Breakpoint at which the widget expands to reveal USB and Wireless debugging buttons. */
        val EXPANDED_WIDTH_THRESHOLD = 195.dp
    }
}

@Composable
private fun AdaptiveWidgetBody(
    devEnabled: Boolean,
    usbEnabled: Boolean,
    wirelessEnabled: Boolean,
) {
    val size = LocalSize.current
    val isExpanded = size.width >= LoopholeWidget.EXPANDED_WIDTH_THRESHOLD

    if (isExpanded) {
        ExpandedWidgetContent(devEnabled, usbEnabled, wirelessEnabled)
    } else {
        CompactWidgetContent(devEnabled)
    }
}

@Composable
private fun CompactWidgetContent(enabled: Boolean) {
    val containerBg = if (enabled) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surfaceVariant
    val primaryText = if (enabled) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurfaceVariant
    val accentBg = if (enabled) GlanceTheme.colors.primary else GlanceTheme.colors.surface
    val accentFg = if (enabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant

    val context = LocalContext.current
    val toggleDevIntent = ToggleReceiver.createToggleIntent(context, SecureSetting.DEV_OPTIONS)

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(containerBg)
            .cornerRadius(24.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle touch target
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .clickable(actionSendBroadcast(toggleDevIntent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .cornerRadius(16.dp)
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_dev_mode_tile),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(accentFg),
                    modifier = GlanceModifier.size(18.dp)
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            Column(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Dev Mode",
                    style = TextStyle(
                        color = primaryText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = if (enabled) "ON" else "OFF",
                    style = TextStyle(
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }

        Spacer(GlanceModifier.width(8.dp))

        // Settings gear button
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(18.dp)
                .background(accentBg)
                .clickable(actionStartActivity<DevOptionsActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_settings_gear),
                contentDescription = "Open Developer Options",
                colorFilter = ColorFilter.tint(accentFg),
                modifier = GlanceModifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ExpandedWidgetContent(devEnabled: Boolean, usbEnabled: Boolean, wirelessEnabled: Boolean) {
    val containerBg = GlanceTheme.colors.surfaceVariant

    val context = LocalContext.current
    val toggleDevIntent = ToggleReceiver.createToggleIntent(context, SecureSetting.DEV_OPTIONS)
    val toggleUsbIntent = ToggleReceiver.createToggleIntent(context, SecureSetting.USB_DEBUGGING)
    val toggleWirelessIntent = ToggleReceiver.createToggleIntent(context, SecureSetting.WIRELESS_DEBUGGING)

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(containerBg)
            .cornerRadius(24.dp)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dev Mode Segment
        WidgetActionSegment(
            title = "Dev",
            statusText = if (devEnabled) "ON" else "OFF",
            iconRes = R.drawable.ic_dev_mode_tile,
            isActive = devEnabled,
            onClick = toggleDevIntent,
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(GlanceModifier.width(6.dp))

        // USB Debugging Segment
        WidgetActionSegment(
            title = "USB",
            statusText = if (devEnabled && usbEnabled) "ON" else "OFF",
            iconRes = R.drawable.ic_usb_tile,
            isActive = devEnabled && usbEnabled,
            isEnabled = devEnabled,
            onClick = toggleUsbIntent,
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(GlanceModifier.width(6.dp))

        // Wireless Debugging Segment
        WidgetActionSegment(
            title = "Wireless",
            statusText = if (devEnabled && wirelessEnabled) "ON" else "OFF",
            iconRes = R.drawable.ic_wireless_tile,
            isActive = devEnabled && wirelessEnabled,
            isEnabled = devEnabled,
            onClick = toggleWirelessIntent,
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(GlanceModifier.width(6.dp))

        // Settings Shortcut Button
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(20.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<DevOptionsActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_settings_gear),
                contentDescription = "Open Developer Options",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WidgetActionSegment(
    title: String,
    statusText: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: Intent,
    isEnabled: Boolean = true,
    modifier: GlanceModifier = GlanceModifier,
) {
    val bg = if (isActive && isEnabled) GlanceTheme.colors.primary else GlanceTheme.colors.surface
    val fg = if (isActive && isEnabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurfaceVariant

    val clickModifier = if (isEnabled) {
        GlanceModifier.clickable(actionSendBroadcast(onClick))
    } else {
        GlanceModifier
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .background(bg)
            .cornerRadius(18.dp)
            .then(clickModifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = title,
            colorFilter = ColorFilter.tint(fg),
            modifier = GlanceModifier.size(16.dp)
        )
        Spacer(GlanceModifier.width(6.dp))
        Column(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = TextStyle(
                    color = fg,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp
                )
            )
            Text(
                text = statusText,
                style = TextStyle(
                    color = fg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

class LoopholeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoopholeWidget()
}
