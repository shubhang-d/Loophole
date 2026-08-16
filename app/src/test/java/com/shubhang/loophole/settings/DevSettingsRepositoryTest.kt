package com.shubhang.loophole.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevSettingsRepositoryTest {

    private val source = FakeSecureSettingsSource()
    private val changes = mutableListOf<Boolean>()

    private val repository = DevSettingsRepository(
        source = source,
        ioDispatcher = UnconfinedTestDispatcher(),
        onChanged = { changes += it },
    )

    @Test
    fun `setEnabled writes the value and reports success`() = runTest {
        val result = repository.setEnabled(true)

        assertEquals(SettingsWriteResult.Success(isEnabled = true), result)
        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
    }

    @Test
    fun `setEnabled reports PermissionDenied and leaves the value alone`() = runTest {
        source.writesSucceed = false

        val result = repository.setEnabled(true)

        assertEquals(SettingsWriteResult.PermissionDenied, result)
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
    }

    @Test
    fun `toggle flips the stored value`() = runTest {
        repository.setEnabled(true)

        assertEquals(SettingsWriteResult.Success(isEnabled = false), repository.toggle())
        assertEquals(SettingsWriteResult.Success(isEnabled = true), repository.toggle())
    }

    @Test
    fun `a successful write reports the stored value to onChanged`() = runTest {
        repository.setEnabled(true)
        repository.setEnabled(false)

        assertEquals(listOf(true, false), changes)
    }

    @Test
    fun `a denied write reports nothing to onChanged`() = runTest {
        source.writesSucceed = false

        repository.setEnabled(true)

        assertTrue(changes.isEmpty())
    }

    @Test
    fun `enabling clears both debugging flags before switching dev options on`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, true)

        repository.setEnabled(true)

        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
        // Order matters: debugging must be off before dev options comes on.
        assertEquals(
            listOf(
                SecureSetting.USB_DEBUGGING to false,
                SecureSetting.WIRELESS_DEBUGGING to false,
                SecureSetting.DEV_OPTIONS to true,
            ),
            source.writes
        )
    }

    @Test
    fun `disabling leaves the debugging flags untouched`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)

        repository.setEnabled(false)

        assertEquals(listOf(SecureSetting.DEV_OPTIONS to false), source.writes)
        assertTrue(source.read(SecureSetting.USB_DEBUGGING))
    }

    @Test
    fun `toggling on also clears the debugging flags`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, true)

        repository.toggle()

        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `a denied debugging write stops before dev options is touched`() = runTest {
        source.writesSucceed = false

        val result = repository.setEnabled(true)

        assertEquals(SettingsWriteResult.PermissionDenied, result)
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
        assertTrue(source.writes.isEmpty())
    }

    @Test
    fun `isEnabled reflects changes made outside the app`() = runTest {
        assertFalse(repository.isEnabled.first())

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)

        assertTrue(repository.isEnabled.first())
    }
}
