package bd.ac.du.fm.noticetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey
    val id: String,
    val title: String,
    val date: String,
    val link: String,
    val source: String,
    val firstSeenAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
