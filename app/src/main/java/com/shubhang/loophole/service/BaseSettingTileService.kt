package com.shubhang.loophole.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.StringRes
import com.shubhang.loophole.appContainer
import com.shubhang.loophole.settings.SecureSetting
import com.shubhang.loophole.settings.SettingsWriteResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Base Quick Settings tile service providing lifecycle management, live setting observation,
 * write error fallback, and app navigation.
 */
abstract class BaseSettingTileService(
    private val setting: SecureSetting,
    @param:StringRes private val labelOnRes: Int,
    @param:StringRes private val labelOffRes: Int,
    @param:StringRes private val labelUnsupportedRes: Int = labelOffRes,
) : TileService() {

    private val devSettings by lazy { appContainer.devSettings }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listeningJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        if (!setting.isSupportedOnCurrentSdk) {
            renderUnsupported()
            return
        }
        listeningJob = serviceScope.launch {
            if (setting in SecureSetting.DEBUGGING) {
                combine(
                    devSettings.observe(SecureSetting.DEV_OPTIONS),
                    devSettings.observe(setting)
                ) { devEnabled, settingEnabled ->
                    devEnabled && settingEnabled
                }.collect(::renderTile)
            } else {
                devSettings.observe(setting).collect(::renderTile)
            }
        }
    }

    override fun onStopListening() {
        listeningJob?.cancel()
        listeningJob = null
        super.onStopListening()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()
        if (!setting.isSupportedOnCurrentSdk) return

        serviceScope.launch {
            if (setting in SecureSetting.DEBUGGING && !devSettings.currentValue(SecureSetting.DEV_OPTIONS)) {
                openApp()
                return@launch
            }

            if (devSettings.toggle(setting) is SettingsWriteResult.PermissionDenied) {
                openApp()
            }
        }
    }

    protected open fun renderUnsupported() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_UNAVAILABLE
        tile.label = getString(labelUnsupportedRes)
        tile.updateTile()
    }

    protected open fun renderTile(enabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(if (enabled) labelOnRes else labelOffRes)
        tile.updateTile()
    }

    /** Opens the app so the user can read the WRITE_SECURE_SETTINGS grant instructions. */
    @SuppressLint("StartActivityAndCollapseDeprecated")
    protected fun openApp() {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return
        startActivityAndCollapseSafely(launch)
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startActivityAndCollapseSafely(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
