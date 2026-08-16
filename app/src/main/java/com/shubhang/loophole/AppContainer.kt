package com.shubhang.loophole

import android.content.Context
import com.shubhang.loophole.settings.AndroidSecureSettingsSource
import com.shubhang.loophole.settings.DevSettingsRepository
import com.shubhang.loophole.settings.DeveloperOptionsLauncher
import com.shubhang.loophole.settings.QuickSettingsTileManager
import com.shubhang.loophole.widget.WidgetUpdater

/**
 * The app's dependency graph, in one readable place. Everything here is
 * app-lifetime and created lazily on first use.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val secureSettings by lazy { AndroidSecureSettingsSource(appContext) }

    private val widgetUpdater by lazy { WidgetUpdater(appContext) }

    /**
     * The widget cannot subscribe to a Flow — it is only alive while being
     * rendered — so every successful write pushes the new value into it here,
     * rather than at each of the three call sites that toggle.
     */
    val devSettings: DevSettingsRepository by lazy {
        DevSettingsRepository(
            source = secureSettings,
            onChanged = { widgetUpdater.refresh(it) },
        )
    }

    val developerOptionsLauncher by lazy { DeveloperOptionsLauncher(appContext) }

    val quickSettingsTileManager by lazy { QuickSettingsTileManager(appContext) }
}

/** Reaches the graph from any Context — activities, services, receivers, workers. */
val Context.appContainer: AppContainer
    get() = (applicationContext as LoopholeApplication).container
