package com.shubhang.loophole.widget

import android.content.Context
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs against the real [com.shubhang.loophole.LoopholeApplication], so it
 * verifies the part of the wiring that only fails at runtime: that the widget's
 * broadcast path reaches the repository through the app container and actually
 * flips the setting.
 *
 * Requires WRITE_SECURE_SETTINGS:
 *   adb shell pm grant com.shubhang.loophole android.permission.WRITE_SECURE_SETTINGS
 */
@RunWith(AndroidJUnit4::class)
class ToggleWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /**
     * Gradle reinstalls the app for each run, which drops the adb grant, so the
     * test grants it itself through the instrumentation shell.
     */
    @Before
    fun grantWriteSecureSettings() {
        val command = "pm grant ${context.packageName} " +
            "android.permission.WRITE_SECURE_SETTINGS"
        val descriptor = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)
        // Draining to EOF is what waits for the command to finish.
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    @Test
    fun toggleWorkerFlipsDeveloperOptions() {
        val before = isDevModeEnabled()

        ToggleWorker.enqueue(context)
        val info = awaitTerminalWorkInfo()

        assertEquals(WorkInfo.State.SUCCEEDED, info.state)
        assertEquals(!before, isDevModeEnabled())

        // Leave the device as we found it.
        ToggleWorker.enqueue(context)
        awaitTerminalWorkInfo()
        assertEquals(before, isDevModeEnabled())
    }

    private fun isDevModeEnabled(): Boolean =
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1

    private fun awaitTerminalWorkInfo(): WorkInfo {
        val workManager = WorkManager.getInstance(context)
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            val info = workManager
                .getWorkInfosForUniqueWork(UNIQUE_WORK_NAME)
                .get()
                .firstOrNull()
            if (info != null && info.state.isFinished) return info
            Thread.sleep(POLL_MILLIS)
        }
        error("ToggleWorker did not finish within ${TIMEOUT_MILLIS}ms")
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "toggle_dev_mode"
        const val TIMEOUT_MILLIS = 10_000L
        const val POLL_MILLIS = 100L
    }
}
