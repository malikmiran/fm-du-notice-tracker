package bd.ac.du.fm.noticetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bd.ac.du.fm.noticetracker.data.model.Notice
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY firstSeenAt DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Query("SELECT * FROM notices WHERE id = :id LIMIT 1")
    suspend fun getNoticeById(id: String): Notice?

    @Query("SELECT id FROM notices")
    suspend fun getAllNoticeIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotices(notices: List<Notice>): List<Long>

    @Query("UPDATE notices SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notices SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("SELECT COUNT(*) FROM notices WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("DELETE FROM notices")
    suspend fun clearAll()
}
