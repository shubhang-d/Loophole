package com.shubhang.loophole.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shubhang.loophole.DevMode

/**
 * Robust background worker that toggles Developer Options and refreshes the widgets.
 * Using WorkManager ensures the task completes even if the app process is under pressure.
 */
class ToggleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val ok = DevMode.toggle(context)
        Log.d("Loophole", "ToggleWorker: DevMode.toggle returned $ok")
        refreshLoopholeWidgets(context)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "toggle_dev_mode"

        fun enqueue(context: Context) {
            Log.d("Loophole", "ToggleWorker.enqueue called")
            val request = OneTimeWorkRequestBuilder<ToggleWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
