package bd.ac.du.fm.noticetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.ac.du.fm.noticetracker.data.model.Notice
import bd.ac.du.fm.noticetracker.data.repository.NoticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoticeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoticeRepository(application)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _lastChecked = MutableStateFlow(repository.getLastCheckedTime())
    val lastChecked: StateFlow<Long> = _lastChecked

    private val allNoticesFlow = repository.getAllNotices()

    val notices: StateFlow<List<Notice>> = combine(allNoticesFlow, _selectedFilter) { list, filter ->
        when (filter) {
            "MBBS" -> list.filter { it.source == "MBBS Category" }
            "Ticker" -> list.filter { it.source == "Homepage Ticker" }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        refresh()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.checkForUpdates()
                _lastChecked.value = repository.getLastCheckedTime()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }
}
