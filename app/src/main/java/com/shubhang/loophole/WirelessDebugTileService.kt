package com.shubhang.loophole

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.shubhang.loophole.widget.refreshLoopholeWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Quick Settings tile that toggles Android's Wireless Debugging (`adb_wifi_enabled`).
 * Requires Android 11+ (API 30+).
 */
class WirelessDebugTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        if (!WirelessDebug.isSupported()) {
            openApp()
            return
        }
        val ok = WirelessDebug.toggle(this)
        if (!ok) openApp()
        refreshTile()
        val appContext = applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            refreshLoopholeWidgets(appContext)
        }
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        if (!WirelessDebug.isSupported()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = getString(R.string.wireless_not_supported)
            tile.updateTile()
            return
        }
        val enabled = WirelessDebug.isEnabled(this)
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(if (enabled) R.string.wireless_tile_label_on else R.string.wireless_tile_label_off)
        tile.updateTile()
    }

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
