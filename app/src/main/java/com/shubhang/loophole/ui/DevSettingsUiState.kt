package com.shubhang.loophole.ui

/** The toggle state [LoopholeScreen] renders. */
data class DevSettingsUiState(
    val isEnabled: Boolean = false,
    /** Set after a write that WRITE_SECURE_SETTINGS blocked. */
    val isPermissionDenied: Boolean = false,
)
