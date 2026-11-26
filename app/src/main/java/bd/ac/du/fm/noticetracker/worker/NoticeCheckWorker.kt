package bd.ac.du.fm.noticetracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bd.ac.du.fm.noticetracker.R
import bd.ac.du.fm.noticetracker.data.model.Notice
import bd.ac.du.fm.noticetracker.data.repository.NoticeRepository
import bd.ac.du.fm.noticetracker.ui.MainActivity

class NoticeCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = NoticeRepository(context)

    override suspend fun doWork(): Result {
        return try {
            val newNotices = repository.checkForUpdates()
            if (newNotices.isNotEmpty()) {
                sendNotifications(newNotices)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendNotifications(notices: List<Notice>) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure Channel exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FM DU New Notices",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a new notice is published by DU Faculty of Medicine"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Send notifications
        notices.take(5).forEachIndexed { index, notice ->
            val pdfIntent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.link)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notice.id.hashCode(),
                pdfIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New FM DU Notice: ${notice.date}")
                .setContentText(notice.title)
                .setStyle(NotificationCompat.BigTextStyle().bigText("${notice.title}\n\nSource: ${notice.source}\nTap to open PDF."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify(notice.id.hashCode(), builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID = "fm_du_notices_alerts"
        const val WORK_TAG = "fm_du_periodic_checker"
    }
}
