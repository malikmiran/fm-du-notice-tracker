package bd.ac.du.fm.noticetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import bd.ac.du.fm.noticetracker.worker.NoticeCheckWorker
import java.util.concurrent.TimeUnit

class NoticeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        schedulePeriodicNoticeCheck()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NoticeCheckWorker.CHANNEL_ID,
                "FM DU New Notices",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new Faculty of Medicine notices"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun schedulePeriodicNoticeCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<NoticeCheckWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // flex interval
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NoticeCheckWorker.WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }
}
