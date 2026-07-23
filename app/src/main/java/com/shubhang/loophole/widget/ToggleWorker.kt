package com.shubhang.loophole.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shubhang.loophole.DevMode
import com.shubhang.loophole.ToggleTarget
import com.shubhang.loophole.UsbDebug
import com.shubhang.loophole.WirelessDebug

/**
 * Background worker that toggles the selected setting and refreshes the widgets.
 */
class ToggleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val targetName = inputData.getString(KEY_TARGET) ?: ToggleTarget.DEV_MODE.name
        val target = try {
            ToggleTarget.valueOf(targetName)
        } catch (e: Exception) {
            ToggleTarget.DEV_MODE
        }

        val ok = when (target) {
            ToggleTarget.DEV_MODE -> DevMode.toggle(context)
            ToggleTarget.USB_DEBUG -> UsbDebug.toggle(context)
            ToggleTarget.WIRELESS_DEBUG -> WirelessDebug.toggle(context)
        }

        Log.d("Loophole", "ToggleWorker ($target): toggle returned $ok")
        refreshLoopholeWidgets(context)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME_PREFIX = "toggle_"
        private const val KEY_TARGET = "key_target"

        fun enqueue(context: Context, target: ToggleTarget = ToggleTarget.DEV_MODE) {
            Log.d("Loophole", "ToggleWorker.enqueue called for $target")
            val inputData = Data.Builder()
                .putString(KEY_TARGET, target.name)
                .build()

            val request = OneTimeWorkRequestBuilder<ToggleWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$UNIQUE_WORK_NAME_PREFIX${target.name.lowercase()}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
