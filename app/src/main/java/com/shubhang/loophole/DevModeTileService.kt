package com.shubhang.loophole

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings tile that toggles Android's Developer Options.
 *
 * A single tap flips the state. If WRITE_SECURE_SETTINGS has not been granted
 * the write fails silently at the system level, so the tile opens the app
 * instead to show the adb-grant instructions.
 *
 * Note: the long-press action of a custom Quick Settings tile is controlled by
 * the system (it opens the owning app) and cannot be redirected by the app, so
 * "open Developer Options" is offered from inside the app and the widget rather
 * than via long-press here.
 */
class DevModeTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val ok = DevMode.toggle(this)
        if (!ok) openApp()
        refreshTile()
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val enabled = DevMode.isEnabled(this)
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
