package com.shubhang.loophole.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for the framework settings store.
 *
 * [writesSucceed] models whether WRITE_SECURE_SETTINGS has been granted: when
 * false, writes are rejected and leave the stored value untouched, exactly as
 * the real source behaves when it catches a SecurityException.
 */
class FakeSecureSettingsSource(
    initialValues: Map<SecureSetting, Boolean> = emptyMap(),
    var writesSucceed: Boolean = true,
) : SecureSettingsSource {

    private val values = MutableStateFlow(
        SecureSetting.entries.associateWith { initialValues[it] ?: false }
    )

    /** Every accepted write, in order, so callers can assert sequencing. */
    val writes = mutableListOf<Pair<SecureSetting, Boolean>>()

    override fun read(setting: SecureSetting): Boolean = values.value.getValue(setting)

    override fun write(setting: SecureSetting, enabled: Boolean): Boolean {
        if (!writesSucceed) return false
        writes += setting to enabled
        values.value = values.value + (setting to enabled)
        return true
    }

    override fun observe(setting: SecureSetting): Flow<Boolean> =
        values.map { it.getValue(setting) }

    /** Simulates a change made outside the app (system Settings, adb). */
    fun emitExternalChange(setting: SecureSetting, enabled: Boolean) {
        values.value = values.value + (setting to enabled)
    }
}
