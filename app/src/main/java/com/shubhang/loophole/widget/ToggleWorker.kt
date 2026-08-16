package com.shubhang.loophole.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shubhang.loophole.appContainer

/**
 * Toggles Developer Options off the widget's broadcast. Using WorkManager
 * ensures the task completes even if the app process is under pressure.
 *
 * The repository refreshes the widgets itself after a successful write, so
 * there is nothing further to do here.
 */
class ToggleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        applicationContext.appContainer.devSettings.toggle()
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "toggle_dev_mode"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<ToggleWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
