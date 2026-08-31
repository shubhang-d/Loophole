package com.shubhang.loophole.ui

import com.shubhang.loophole.settings.SecureSetting

/** The toggle state [LoopholeScreen] renders. */
data class DevSettingsUiState(
    val isEnabled: Boolean = false,
    val isUsbDebuggingEnabled: Boolean = false,
    val isWirelessDebuggingEnabled: Boolean = false,
    val isWirelessDebuggingSupported: Boolean = SecureSetting.WIRELESS_DEBUGGING.isSupportedOnCurrentSdk,
    /** Set after a write that WRITE_SECURE_SETTINGS blocked. */
    val isPermissionDenied: Boolean = false,
)
