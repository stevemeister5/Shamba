package com.shambasmart.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.FeedDao
import com.shambasmart.data.local.dao.SilageDao
import com.shambasmart.data.local.entity.FeedInventory
import com.shambasmart.data.local.entity.SilageInventory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedDao: FeedDao,
    private val silageDao: SilageDao
) : ViewModel() {

    val allFeedInventory: StateFlow<List<FeedInventory>> = feedDao.getAllFeedInventory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeed: StateFlow<List<FeedInventory>> = allFeedInventory // Alias for UI compatibility

    val allSilageInventory: StateFlow<List<SilageInventory>> = silageDao.getAllSilage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSilage: StateFlow<List<SilageInventory>> = allSilageInventory // Alias for UI compatibility

    fun addFeed(feed: FeedInventory) {
        viewModelScope.launch {
            feedDao.insert(feed)
        }
    }

    fun updateFeed(feed: FeedInventory) {
        viewModelScope.launch {
            feedDao.update(feed.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteFeed(feed: FeedInventory) {
        viewModelScope.launch {
            feedDao.delete(feed)
        }
    }

    fun addSilage(silage: SilageInventory) {
        viewModelScope.launch {
            silageDao.insert(silage)
        }
    }

    fun updateSilage(silage: SilageInventory) {
        viewModelScope.launch {
            silageDao.update(silage.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteSilage(silage: SilageInventory) {
        viewModelScope.launch {
            silageDao.delete(silage)
        }
    }
}
