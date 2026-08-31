package com.shubhang.loophole.settings

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.shubhang.loophole.R
import com.shubhang.loophole.service.DevModeTileService
import com.shubhang.loophole.service.UsbDebugTileService
import com.shubhang.loophole.service.WirelessDebugTileService

enum class TileType {
    DEV_MODE,
    USB_DEBUG,
    WIRELESS_DEBUG
}

/**
 * Prompts the system to add tiles to Quick Settings via a one-tap
 * dialog (API 33+). This avoids relying on the user finding it in the QS editor,
 * where a freshly installed custom tile can take a SystemUI restart to appear.
 */
class QuickSettingsTileManager(context: Context) {

    private val appContext = context.applicationContext

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * [onResult] is delivered on the main thread. On releases without the
     * add-tile dialog it reports [AddTileResult.FAILED] rather than silently
     * doing nothing.
     */
    fun requestAddTile(onResult: (AddTileResult) -> Unit) {
        requestAddTile(TileType.DEV_MODE, onResult)
    }

    fun requestAddTile(tileType: TileType, onResult: (AddTileResult) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestAddTileApi33(tileType, onResult)
        } else {
            onResult(AddTileResult.FAILED)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestAddTileApi33(tileType: TileType, onResult: (AddTileResult) -> Unit) {
        val statusBar = appContext.getSystemService(StatusBarManager::class.java)
        if (statusBar == null) {
            onResult(AddTileResult.FAILED)
            return
        }

        val (serviceClass, labelRes, iconRes) = when (tileType) {
            TileType.DEV_MODE -> Triple(DevModeTileService::class.java, R.string.tile_label, R.drawable.ic_dev_mode_tile)
            TileType.USB_DEBUG -> Triple(UsbDebugTileService::class.java, R.string.tile_usb_label, R.drawable.ic_usb_tile)
            TileType.WIRELESS_DEBUG -> Triple(WirelessDebugTileService::class.java, R.string.tile_wireless_label, R.drawable.ic_wireless_tile)
        }

        statusBar.requestAddTileService(
            ComponentName(appContext, serviceClass),
            appContext.getString(labelRes),
            Icon.createWithResource(appContext, iconRes),
            appContext.mainExecutor
        ) { resultCode ->
            onResult(resultCode.toAddTileResult())
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun Int.toAddTileResult(): AddTileResult = when (this) {
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> AddTileResult.ADDED
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> AddTileResult.ALREADY_ADDED
        // The user saw the dialog and closed it without adding.
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> AddTileResult.DISMISSED
        // The remaining codes are all TILE_ADD_REQUEST_ERROR_*.
        else -> AddTileResult.FAILED
    }
}
