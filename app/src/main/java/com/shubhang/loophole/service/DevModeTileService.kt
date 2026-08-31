package com.shubhang.loophole.service

import com.shubhang.loophole.R
import com.shubhang.loophole.settings.SecureSetting

/**
 * Quick Settings tile that toggles Android's Developer Options.
 */
class DevModeTileService : BaseSettingTileService(
    setting = SecureSetting.DEV_OPTIONS,
    labelOnRes = R.string.tile_label_on,
    labelOffRes = R.string.tile_label_off,
)
