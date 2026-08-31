package com.shubhang.loophole.service

import com.shubhang.loophole.R
import com.shubhang.loophole.settings.SecureSetting

/**
 * Quick Settings tile that toggles USB Debugging.
 */
class UsbDebugTileService : BaseSettingTileService(
    setting = SecureSetting.USB_DEBUGGING,
    labelOnRes = R.string.tile_usb_label_on,
    labelOffRes = R.string.tile_usb_label_off,
)
