package com.shambasmart.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.FeedDao
import com.shambasmart.data.local.entity.FeedInventory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedDao: FeedDao
) : ViewModel() {

    val allFeedInventory: StateFlow<List<FeedInventory>> = feedDao.getAllFeedInventory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}