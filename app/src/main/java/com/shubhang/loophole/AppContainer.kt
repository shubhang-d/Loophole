package com.shubhang.loophole

import android.content.Context
import com.shubhang.loophole.settings.AndroidSecureSettingsSource
import com.shubhang.loophole.settings.DevSettingsRepository
import com.shubhang.loophole.settings.DeveloperOptionsLauncher
import com.shubhang.loophole.settings.QuickSettingsTileManager
import com.shubhang.loophole.settings.SharedPreferencesDebuggingStateStore
import com.shubhang.loophole.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * The app's dependency graph, in one readable place. Everything here is
 * app-lifetime and created lazily on first use.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val secureSettings by lazy { AndroidSecureSettingsSource(appContext) }

    val widgetUpdater: WidgetUpdater by lazy { WidgetUpdater(appContext) }

    private val debugStateStore by lazy {
        SharedPreferencesDebuggingStateStore(
            appContext.getSharedPreferences("dev_settings_backup", Context.MODE_PRIVATE)
        )
    }

    /**
     * Unified setting repository. State changes across all three settings
     * (whether in-app, from QS tiles, via ADB, or in system Settings) trigger
     * widget refresh via the application-scoped observer below.
     */
    val devSettings: DevSettingsRepository by lazy {
        DevSettingsRepository(
            source = secureSettings,
            stateStore = debugStateStore,
            onChanged = { widgetUpdater.refresh() }
        ).also { repo ->
            appScope.launch {
                combine(
                    repo.isEnabled,
                    repo.isUsbDebuggingEnabled,
                    repo.isWirelessDebuggingEnabled
                ) { _, _, _ -> }
                    .drop(1)
                    .collect {
                        widgetUpdater.refresh()
                    }
            }
        }
    }

    val developerOptionsLauncher by lazy { DeveloperOptionsLauncher(appContext) }

    val quickSettingsTileManager by lazy { QuickSettingsTileManager(appContext) }
}

/** Reaches the graph from any Context — activities, services, receivers, workers. */
val Context.appContainer: AppContainer
    get() = (applicationContext as LoopholeApplication).container
