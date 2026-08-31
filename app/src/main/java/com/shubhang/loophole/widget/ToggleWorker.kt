package com.shubhang.loophole.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.shubhang.loophole.appContainer
import com.shubhang.loophole.settings.SecureSetting

/**
 * Toggles settings off the widget's broadcast. Using WorkManager
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
        val settingName = inputData.getString(KEY_SETTING) ?: SecureSetting.DEV_OPTIONS.name
        val setting = runCatching { SecureSetting.valueOf(settingName) }.getOrDefault(SecureSetting.DEV_OPTIONS)
        applicationContext.appContainer.devSettings.toggle(setting)
        applicationContext.appContainer.widgetUpdater.refresh()
        return Result.success()
    }

    companion object {
        const val KEY_SETTING = "setting"
        private const val UNIQUE_WORK_PREFIX = "toggle_setting_"

        fun enqueue(context: Context, settingName: String = SecureSetting.DEV_OPTIONS.name) {
            val request = OneTimeWorkRequestBuilder<ToggleWorker>()
                .setInputData(workDataOf(KEY_SETTING to settingName))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "$UNIQUE_WORK_PREFIX$settingName",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
