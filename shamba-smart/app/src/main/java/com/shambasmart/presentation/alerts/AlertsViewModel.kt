package com.shambasmart.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.domain.model.Alert
import com.shambasmart.domain.usecase.AlertsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertsEngine: AlertsEngine
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _isLoading.value = true
            alertsEngine.generateAlerts().collect { alertList ->
                _alerts.value = alertList
                _isLoading.value = false
            }
        }
    }

    fun dismissAlert(alertId: Long) {
        _alerts.value = _alerts.value.map { alert ->
            if (alert.id == alertId) {
                alert.copy(isDismissed = true, dismissedAt = System.currentTimeMillis())
            } else {
                alert
            }
        }
    }

    fun dismissAllAlerts() {
        val now = System.currentTimeMillis()
        _alerts.value = _alerts.value.map { alert ->
            alert.copy(isDismissed = true, dismissedAt = now)
        }
    }

    fun refreshAlerts() {
        loadAlerts()
    }
}