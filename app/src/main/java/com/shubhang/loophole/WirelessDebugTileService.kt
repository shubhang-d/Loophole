package com.shubhang.loophole

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.shubhang.loophole.widget.refreshWirelessDebugWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Quick Settings tile that toggles Android's Wireless Debugging.
 */
class WirelessDebugTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        TileSettings.setWirelessTileAdded(this, true)
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        TileSettings.setWirelessTileAdded(this, false)
    }

    override fun onStartListening() {
        super.onStartListening()
        TileSettings.setWirelessTileAdded(this, true)
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val ok = WirelessDevMode.toggle(this)
        if (!ok) openApp()
        refreshTile()
        val appContext = applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            refreshWirelessDebugWidgets(appContext)
        }
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val enabled = WirelessDevMode.isEnabled(this)
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(if (enabled) R.string.tile_wireless_label_on else R.string.tile_wireless_label_off)
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
