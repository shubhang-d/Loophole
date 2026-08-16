package com.shubhang.loophole.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.shubhang.loophole.R
import com.shubhang.loophole.appContainer
import com.shubhang.loophole.settings.SettingsWriteResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Quick Settings tile that toggles Android's Developer Options.
 *
 * A single tap flips the state. If WRITE_SECURE_SETTINGS has not been granted
 * the write fails, so the tile opens the app instead to show the adb-grant
 * instructions.
 *
 * Long-press opens the app rather than the system App Info screen. That is not
 * configured here: SystemUI resolves ACTION_QS_TILE_PREFERENCES against this
 * package, so the behaviour comes from the intent-filter MainActivity declares
 * in the manifest.
 */
class DevModeTileService : TileService() {

    private val devSettings by lazy { appContainer.devSettings }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listeningJob: Job? = null

    /**
     * While the shade is open, mirror the repository. This keeps the tile
     * correct even when the value changes elsewhere.
     */
    override fun onStartListening() {
        super.onStartListening()
        listeningJob = serviceScope.launch {
            devSettings.isEnabled.collect(::renderTile)
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
        serviceScope.launch {
            if (devSettings.toggle() is SettingsWriteResult.PermissionDenied) {
                openApp()
            }
        }
    }

    private fun renderTile(enabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(if (enabled) R.string.tile_label_on else R.string.tile_label_off)
        tile.updateTile()
    }

    /** Opens the app so the user can read the WRITE_SECURE_SETTINGS grant instructions. */
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun openApp() {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this, 0, launch, PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(launch)
        }
    }
}
