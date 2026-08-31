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
    private val stateStore = InMemoryDebuggingStateStore()
    private val changes = mutableListOf<Boolean>()

    private val repository = DevSettingsRepository(
        source = source,
        ioDispatcher = UnconfinedTestDispatcher(),
        stateStore = stateStore,
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
    fun `disabling dev options stops active debugging flags and records their state`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, false)

        repository.setEnabled(false)

        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
        assertTrue(stateStore.getSavedState(SecureSetting.USB_DEBUGGING))
        assertFalse(stateStore.getSavedState(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `disabling dev options when both debugging flags active stops both and records them`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, true)

        repository.setEnabled(false)

        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
        assertTrue(stateStore.getSavedState(SecureSetting.USB_DEBUGGING))
        assertTrue(stateStore.getSavedState(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `enabling dev options restores previously recorded USB debugging state`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, false)

        // Turn off dev options (which stops USB debugging and saves state)
        repository.setEnabled(false)
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))

        // Turn dev options back on
        repository.setEnabled(true)
        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
        assertTrue(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `enabling dev options restores previously recorded Wireless debugging state`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, false)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, true)

        repository.setEnabled(false)
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))

        repository.setEnabled(true)
        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertTrue(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `enabling dev options restores both debugging flags if both were active`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, true)

        repository.setEnabled(false)
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))

        repository.setEnabled(true)
        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
        assertTrue(source.read(SecureSetting.USB_DEBUGGING))
        assertTrue(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `enabling dev options without prior active debugging leaves flags off`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, false)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, false)

        repository.setEnabled(false)
        repository.setEnabled(true)

        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `toggling dev options off and then on restores previous debugging state`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, false)

        repository.toggle() // Flips DEV_OPTIONS to false
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))

        repository.toggle() // Flips DEV_OPTIONS to true
        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
        assertTrue(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `a denied debugging write during disable stops and leaves dev options alone`() = runTest {
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.writesSucceed = false

        val result = repository.setEnabled(false)

        assertEquals(SettingsWriteResult.PermissionDenied, result)
        assertTrue(source.writes.isEmpty())
    }

    @Test
    fun `isEnabled reflects changes made outside the app`() = runTest {
        assertFalse(repository.isEnabled.first())

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)

        assertTrue(repository.isEnabled.first())
    }

    @Test
    fun `toggling USB debugging writes to USB_DEBUGGING setting and updates stateStore`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)
        val result = repository.toggle(SecureSetting.USB_DEBUGGING)

        assertEquals(SettingsWriteResult.Success(isEnabled = true), result)
        assertTrue(source.read(SecureSetting.USB_DEBUGGING))
        assertTrue(repository.isUsbDebuggingEnabled.first())
        assertTrue(stateStore.getSavedState(SecureSetting.USB_DEBUGGING))
    }

    @Test
    fun `toggling Wireless debugging writes to WIRELESS_DEBUGGING setting and updates stateStore`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)
        val result = repository.toggle(SecureSetting.WIRELESS_DEBUGGING)

        assertEquals(SettingsWriteResult.Success(isEnabled = true), result)
        assertTrue(source.read(SecureSetting.WIRELESS_DEBUGGING))
        assertTrue(repository.isWirelessDebuggingEnabled.first())
        assertTrue(stateStore.getSavedState(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `enabling USB debugging when dev options is off fails to enable and leaves dev options off`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, false)
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, false)

        val result = repository.setEnabled(SecureSetting.USB_DEBUGGING, true)

        assertEquals(SettingsWriteResult.Success(isEnabled = false), result)
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
    }

    @Test
    fun `enabling Wireless debugging when dev options is off fails to enable and leaves dev options off`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, false)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, false)

        val result = repository.setEnabled(SecureSetting.WIRELESS_DEBUGGING, true)

        assertEquals(SettingsWriteResult.Success(isEnabled = false), result)
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
    }

    @Test
    fun `toggling USB debugging when dev options is off has no effect and remains off`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, false)
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, false)

        val result = repository.toggle(SecureSetting.USB_DEBUGGING)

        assertEquals(SettingsWriteResult.Success(isEnabled = false), result)
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
    }

    @Test
    fun `toggling Wireless debugging when dev options is off has no effect and remains off`() = runTest {
        source.emitExternalChange(SecureSetting.DEV_OPTIONS, false)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, false)

        val result = repository.toggle(SecureSetting.WIRELESS_DEBUGGING)

        assertEquals(SettingsWriteResult.Success(isEnabled = false), result)
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
        assertFalse(source.read(SecureSetting.DEV_OPTIONS))
    }
}
