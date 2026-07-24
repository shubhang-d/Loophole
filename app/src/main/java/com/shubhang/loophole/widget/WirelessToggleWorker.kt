package com.shubhang.loophole.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shubhang.loophole.WirelessDevMode
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker that toggles Wireless Debugging and refreshes the widgets.
 */
class WirelessToggleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val ok = WirelessDevMode.toggle(context)
        Log.d("Loophole", "WirelessToggleWorker: WirelessDevMode.toggle returned $ok")
        if (!ok) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "WRITE_SECURE_SETTINGS not granted", Toast.LENGTH_LONG).show()
            }
        }
        refreshWirelessDebugWidgets(context)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "toggle_wireless_debug"

        fun enqueue(context: Context) {
            Log.d("Loophole", "WirelessToggleWorker.enqueue called")
            val request = OneTimeWorkRequestBuilder<WirelessToggleWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
