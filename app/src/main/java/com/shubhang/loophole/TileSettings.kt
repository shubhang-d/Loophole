package com.shubhang.loophole

import android.content.Context

object TileSettings {
    private const val PREFS_NAME = "tile_settings_prefs"
    private const val KEY_DEV_TILE_ADDED = "dev_tile_added"
    private const val KEY_WIRELESS_TILE_ADDED = "wireless_tile_added"

    fun isDevTileAdded(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DEV_TILE_ADDED, false)
    }

    fun setDevTileAdded(context: Context, added: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DEV_TILE_ADDED, added)
            .apply()
    }

    fun isWirelessTileAdded(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WIRELESS_TILE_ADDED, false)
    }

    fun setWirelessTileAdded(context: Context, added: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WIRELESS_TILE_ADDED, added)
            .apply()
    }
}
