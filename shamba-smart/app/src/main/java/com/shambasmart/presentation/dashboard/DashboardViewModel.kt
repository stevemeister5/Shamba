package com.shambasmart.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.Animal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val animalDao: AnimalDao,
    private val milkProductionDao: MilkProductionDao,
    private val financialDao: FinancialDao,
    private val taskDao: TaskDao,
    private val feedDao: FeedDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val herdSize: StateFlow<Int> = animalDao.getActiveAnimalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goatCount: StateFlow<Int> = animalDao.getCountBySpecies("goat")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sheepCount: StateFlow<Int> = animalDao.getCountBySpecies("sheep")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayMilkYield: StateFlow<Double?> = flow {
        val today = LocalDate.now()
        emit(milkProductionDao.getTotalYieldByDate(today))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Load alerts
                val today = LocalDate.now()
                val pendingTasks = taskDao.getPendingTaskCount(today)
                val lowStockFeed = feedDao.getLowStockFeed()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingTaskCount = pendingTasks,
                        lowFeedAlerts = lowStockFeed.size,
                        hasAlerts = pendingTasks > 0 || lowStockFeed.isNotEmpty()
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
    val pendingTaskCount: Int = 0,
    val lowFeedAlerts: Int = 0,
    val hasAlerts: Boolean = false
)