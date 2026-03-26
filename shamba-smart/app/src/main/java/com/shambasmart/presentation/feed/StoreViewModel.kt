package com.shambasmart.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.StoreDao
import com.shambasmart.data.local.entity.StoreItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeDao: StoreDao
) : ViewModel() {

    val allStoreItems: StateFlow<List<StoreItem>> = storeDao.getAllStoreItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStoreItem(item: StoreItem) {
        viewModelScope.launch {
            storeDao.insert(item)
        }
    }

    fun updateStoreItem(item: StoreItem) {
        viewModelScope.launch {
            storeDao.update(item.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteStoreItem(item: StoreItem) {
        viewModelScope.launch {
            storeDao.delete(item)
        }
    }
}