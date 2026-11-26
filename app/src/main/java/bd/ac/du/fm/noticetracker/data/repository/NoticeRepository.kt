package bd.ac.du.fm.noticetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import bd.ac.du.fm.noticetracker.data.local.AppDatabase
import bd.ac.du.fm.noticetracker.data.model.Notice
import bd.ac.du.fm.noticetracker.data.scraper.NoticeScraper
import kotlinx.coroutines.flow.Flow

class NoticeRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val noticeDao = db.noticeDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("notice_tracker_prefs", Context.MODE_PRIVATE)

    fun getAllNotices(): Flow<List<Notice>> = noticeDao.getAllNotices()
    fun getUnreadCount(): Flow<Int> = noticeDao.getUnreadCount()

    suspend fun markAsRead(id: String) = noticeDao.markAsRead(id)
    suspend fun markAllAsRead() = noticeDao.markAllAsRead()

    /**
     * Checks for updates from the website.
     * Returns a list of newly discovered notices.
     */
    suspend fun checkForUpdates(): List<Notice> {
        val remoteNotices = NoticeScraper.fetchAllNotices()
        if (remoteNotices.isEmpty()) return emptyList()

        val isFirstRun = prefs.getBoolean("is_first_sync_done", false)
        val existingIds = noticeDao.getAllNoticeIds().toSet()

        val brandNewNotices = remoteNotices.filter { it.id !in existingIds }

        // Save all fetched notices to DB
        noticeDao.insertNotices(remoteNotices)

        // Mark last check time
        prefs.edit()
            .putLong("last_checked_timestamp", System.currentTimeMillis())
            .apply()

        return if (!isFirstRun) {
            // First time sync: save existing notices into database without blasting notifications
            prefs.edit().putBoolean("is_first_sync_done", true).apply()
            emptyList()
        } else {
            // Subsequent runs: return newly discovered notices
            brandNewNotices
        }
    }

    fun getLastCheckedTime(): Long {
        return prefs.getLong("last_checked_timestamp", 0L)
    }
}
