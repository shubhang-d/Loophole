package com.shubhang.loophole.settings

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/** Reads and writes [Settings.Global] flags. */
class AndroidSecureSettingsSource(context: Context) : SecureSettingsSource {

    private val appContext = context.applicationContext

    override fun read(setting: SecureSetting): Boolean =
        Settings.Global.getInt(appContext.contentResolver, setting.key, 0) == 1

    override fun write(setting: SecureSetting, enabled: Boolean): Boolean = try {
        Settings.Global.putInt(appContext.contentResolver, setting.key, if (enabled) 1 else 0)
    } catch (e: SecurityException) {
        Log.e(
            TAG,
            "Cannot write ${setting.key} — WRITE_SECURE_SETTINGS not granted. Run: " +
                "adb shell pm grant ${appContext.packageName} " +
                "android.permission.WRITE_SECURE_SETTINGS",
            e
        )
        false
    }

    /**
     * Bridges the framework's [ContentObserver] to a Flow, so the app, tile,
     * and widget pick up changes made anywhere on the device without polling
     * or re-reading on resume.
     */
    override fun observe(setting: SecureSetting): Flow<Boolean> = callbackFlow {
        trySend(read(setting))

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(read(setting))
            }
        }
        appContext.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(setting.key),
            false,
            observer
        )
        awaitClose { appContext.contentResolver.unregisterContentObserver(observer) }
    }
        // Only the latest value matters; dropping intermediate emissions keeps a
        // burst of writes from suspending the observer.
        .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private companion object {
        const val TAG = "Loophole"
    }
}
