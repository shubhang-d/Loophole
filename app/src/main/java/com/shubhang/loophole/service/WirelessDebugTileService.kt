package com.shubhang.loophole.service

import com.shubhang.loophole.R
import com.shubhang.loophole.settings.SecureSetting

/**
 * Quick Settings tile that toggles Wireless Debugging (Android 11+).
 */
class WirelessDebugTileService : BaseSettingTileService(
    setting = SecureSetting.WIRELESS_DEBUGGING,
    labelOnRes = R.string.tile_wireless_label_on,
    labelOffRes = R.string.tile_wireless_label_off,
    labelUnsupportedRes = R.string.tile_wireless_label,
)
