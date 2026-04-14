package com.shambasmart.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.WeatherDao
import com.shambasmart.data.local.entity.WeatherLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherDao: WeatherDao
) : ViewModel() {

    val allWeatherLogs: StateFlow<List<WeatherLog>> = weatherDao.getAllWeatherLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWeatherLog(weatherLog: WeatherLog) {
        viewModelScope.launch {
            weatherDao.insert(weatherLog)
        }
    }
}
