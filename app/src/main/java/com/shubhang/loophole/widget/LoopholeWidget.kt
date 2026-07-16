package com.shubhang.loophole.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.shubhang.loophole.DevMode
import com.shubhang.loophole.DevOptionsActivity
import com.shubhang.loophole.R

// Opaque chip colours that read well on both light and dark launchers.
private val EnabledBackground = ColorProvider(Color(0xFF2E7D46))
private val EnabledForeground = ColorProvider(Color(0xFFFFFFFF))
private val DisabledBackground = ColorProvider(Color(0xFF44484C))
private val DisabledForeground = ColorProvider(Color(0xFFE3E3E3))

// Backing state for the widget. Driven from the real Developer Options setting,
// but stored in Glance state so recomposition (after a tap) actually re-renders.
private val EnabledKey = booleanPreferencesKey("dev_options_enabled")

/**
 * Home-screen widget. Tapping the main area toggles Developer Options; tapping
 * the gear opens the Developer Options screen.
 *
 * A widget's long-press is reserved by the launcher (for moving/resizing), so
 * "open Developer Options" is a dedicated tap target rather than a long-press.
 */
class LoopholeWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Seed widget state from the actual system setting on (re)load.
        updateAppWidgetState(context, id) { prefs ->
            prefs[EnabledKey] = DevMode.isEnabled(context)
        }
        provideContent {
            val enabled = currentState(EnabledKey) ?: false
            WidgetBody(enabled)
        }
    }
}

@Composable
private fun WidgetBody(enabled: Boolean) {
    val background = if (enabled) EnabledBackground else DisabledBackground
    val foreground = if (enabled) EnabledForeground else DisabledForeground

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(background)
            .cornerRadius(24.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main area: tap to toggle.
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(actionRunCallback<ToggleDevModeAction>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_dev_mode_tile),
                contentDescription = null,
                colorFilter = ColorFilter.tint(foreground),
                modifier = GlanceModifier.size(28.dp)
            )
            Spacer(GlanceModifier.width(10.dp))
            Column {
                Text(
                    text = "Dev Mode",
                    style = TextStyle(
                        color = foreground,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = if (enabled) "ON" else "OFF",
                    style = TextStyle(
                        color = foreground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
        }

        // Gear: tap to open Developer Options (via an explicit trampoline activity).
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(20.dp)
                .clickable(actionStartActivity<DevOptionsActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_settings_gear),
                contentDescription = "Open Developer Options",
                colorFilter = ColorFilter.tint(foreground),
                modifier = GlanceModifier.size(22.dp)
            )
        }
    }
}

/** Flips Developer Options, updates widget state, then refreshes the widget. */
class ToggleDevModeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        DevMode.toggle(context)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[EnabledKey] = DevMode.isEnabled(context)
        }
        LoopholeWidget().update(context, glanceId)
    }
}

class LoopholeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LoopholeWidget()
}
