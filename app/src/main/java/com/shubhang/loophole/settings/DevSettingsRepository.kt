package com.shubhang.loophole.settings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Single source of truth for Developer Options. Every entry point — the app UI,
 * the Quick Settings tile, and the home-screen widget — reads and writes
 * through this, so behaviour stays identical everywhere.
 *
 * [onChanged] runs after each successful write. It is how the widget gets
 * refreshed without every caller having to remember to do it; a plain lambda
 * rather than an interface, wired up in
 * [com.shubhang.loophole.AppContainer].
 */
class DevSettingsRepository(
    private val source: SecureSettingsSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onChanged: suspend (Boolean) -> Unit = {},
) {

    /** Current value, then every later change, including changes made outside the app. */
    val isEnabled: Flow<Boolean> =
        source.observe(SecureSetting.DEV_OPTIONS)
            .distinctUntilChanged()
            .flowOn(ioDispatcher)

    suspend fun currentValue(): Boolean = withContext(ioDispatcher) {
        source.read(SecureSetting.DEV_OPTIONS)
    }

    suspend fun setEnabled(enabled: Boolean): SettingsWriteResult = withContext(ioDispatcher) {
        if (enabled) {
            // Clear the debugging flags first, so switching Developer Options on
            // never also restores a USB or wireless debugging session that was
            // left enabled from last time.
            for (setting in SecureSetting.DEBUGGING) {
                if (!source.write(setting, false)) {
                    return@withContext SettingsWriteResult.PermissionDenied
                }
            }
        }

        if (!source.write(SecureSetting.DEV_OPTIONS, enabled)) {
            return@withContext SettingsWriteResult.PermissionDenied
        }
        // Read back rather than trusting the requested value, so callers always
        // reflect what the system actually stored.
        val stored = source.read(SecureSetting.DEV_OPTIONS)
        onChanged(stored)
        SettingsWriteResult.Success(stored)
    }

    suspend fun toggle(): SettingsWriteResult = setEnabled(!currentValue())
}
