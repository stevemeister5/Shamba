package com.shambasmart.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.view.DashboardView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardViewDao: DashboardViewDao,
    private val animalDao: AnimalDao,
    private val taskDao: TaskDao,
    private val feedDao: FeedDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val dashboardData: StateFlow<DashboardView?> = dashboardViewDao.getDashboardData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val herdSize: StateFlow<Int> = animalDao.getActiveAnimalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goatCount: StateFlow<Int> = animalDao.getCountBySpecies("goat")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sheepCount: StateFlow<Int> = animalDao.getCountBySpecies("sheep")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Load dashboard view data
                val dashboardView = dashboardViewDao.getDashboardDataSync()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dashboardView = dashboardView,
                        hasAlerts = (dashboardView?.pendingTasks ?: 0) > 0 || 
                                   (dashboardView?.lowFeedAlerts ?: 0) > 0 ||
                                   (dashboardView?.criticalPestAlerts ?: 0) > 0
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun refreshDashboard() {
        loadDashboardData()
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dashboardView: DashboardView? = null,
    val hasAlerts: Boolean = false
)
