package com.shubhang.loophole.ui

import com.shubhang.loophole.MainDispatcherRule
import com.shubhang.loophole.settings.DevSettingsRepository
import com.shubhang.loophole.settings.FakeSecureSettingsSource
import com.shubhang.loophole.settings.SecureSetting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevSettingsViewModelTest {

    // One dispatcher — and so one scheduler — shared by Dispatchers.Main, the
    // repository, and runTest, so viewModelScope work is driven by the test.
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val source = FakeSecureSettingsSource()

    private val repository = DevSettingsRepository(
        source = source,
        ioDispatcher = testDispatcher,
    )

    private fun viewModel() = DevSettingsViewModel(repository)

    /**
     * uiState is a WhileSubscribed StateFlow, so it only tracks the repository
     * while something collects it. Every test that reads state keeps a
     * collector alive for the duration.
     */
    private fun TestScope.collecting(viewModel: DevSettingsViewModel) =
        backgroundScope.launch { viewModel.uiState.collect { } }

    @Test
    fun `toggle turns developer options on`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        viewModel.onToggle()

        assertTrue(viewModel.uiState.value.isEnabled)
        assertTrue(source.read(SecureSetting.DEV_OPTIONS))
    }

    @Test
    fun `toggle twice returns to the original value`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        viewModel.onToggle()
        viewModel.onToggle()

        assertFalse(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `onToggleUsbDebugging toggles usb debugging state when dev options enabled`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)

        viewModel.onToggleUsbDebugging()

        assertTrue(viewModel.uiState.value.isUsbDebuggingEnabled)
        assertTrue(source.read(SecureSetting.USB_DEBUGGING))

        viewModel.onToggleUsbDebugging()

        assertFalse(viewModel.uiState.value.isUsbDebuggingEnabled)
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
    }

    @Test
    fun `onToggleWirelessDebugging toggles wireless debugging state when dev options enabled`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)

        viewModel.onToggleWirelessDebugging()

        assertTrue(viewModel.uiState.value.isWirelessDebuggingEnabled)
        assertTrue(source.read(SecureSetting.WIRELESS_DEBUGGING))

        viewModel.onToggleWirelessDebugging()

        assertFalse(viewModel.uiState.value.isWirelessDebuggingEnabled)
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `onToggleUsbDebugging does nothing when dev options disabled`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, false)

        viewModel.onToggleUsbDebugging()

        assertFalse(viewModel.uiState.value.isUsbDebuggingEnabled)
        assertFalse(source.read(SecureSetting.USB_DEBUGGING))
    }

    @Test
    fun `onToggleWirelessDebugging does nothing when dev options disabled`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, false)

        viewModel.onToggleWirelessDebugging()

        assertFalse(viewModel.uiState.value.isWirelessDebuggingEnabled)
        assertFalse(source.read(SecureSetting.WIRELESS_DEBUGGING))
    }

    @Test
    fun `a blocked write surfaces the permission card and leaves the state off`() =
        runTest(testDispatcher) {
            source.writesSucceed = false
            val viewModel = viewModel()
            collecting(viewModel)

            viewModel.onToggle()

            assertTrue(viewModel.uiState.value.isPermissionDenied)
            assertFalse(viewModel.uiState.value.isEnabled)
        }

    @Test
    fun `the permission card clears once a write succeeds`() = runTest(testDispatcher) {
        source.writesSucceed = false
        val viewModel = viewModel()
        collecting(viewModel)
        viewModel.onToggle()
        assertTrue(viewModel.uiState.value.isPermissionDenied)

        source.writesSucceed = true
        viewModel.onToggle()

        assertFalse(viewModel.uiState.value.isPermissionDenied)
        assertTrue(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `state follows changes made outside the app`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        collecting(viewModel)

        source.emitExternalChange(SecureSetting.DEV_OPTIONS, true)
        source.emitExternalChange(SecureSetting.USB_DEBUGGING, true)
        source.emitExternalChange(SecureSetting.WIRELESS_DEBUGGING, true)

        assertTrue(viewModel.uiState.value.isEnabled)
        assertTrue(viewModel.uiState.value.isUsbDebuggingEnabled)
        assertTrue(viewModel.uiState.value.isWirelessDebuggingEnabled)
    }
}
