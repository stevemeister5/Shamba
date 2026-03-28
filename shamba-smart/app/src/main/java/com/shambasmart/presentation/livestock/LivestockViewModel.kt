package com.shambasmart.presentation.livestock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.MilkProductionDao
import com.shambasmart.data.local.dao.ReproductionDao
import com.shambasmart.data.local.dao.WeightDao
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.HealthRecord
import com.shambasmart.data.local.entity.MilkProduction
import com.shambasmart.data.local.entity.ReproductionRecord
import com.shambasmart.data.local.entity.WeightEntry
import com.shambasmart.domain.repository.AnimalRepository
import com.shambasmart.domain.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LivestockViewModel @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val reproductionDao: ReproductionDao,
    private val milkProductionDao: MilkProductionDao,
    private val weightDao: WeightDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LivestockUiState())
    val uiState: StateFlow<LivestockUiState> = _uiState.asStateFlow()

    val allAnimals: StateFlow<List<Animal>> = animalRepository.getAllActiveAnimals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val herdSize: StateFlow<Int> = animalRepository.getActiveAnimalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goatCount: StateFlow<Int> = animalRepository.getCountBySpecies("goat")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sheepCount: StateFlow<Int> = animalRepository.getCountBySpecies("Sheep")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cattleCount: StateFlow<Int> = animalRepository.getCountBySpecies("Cattle")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chickenLayerCount: StateFlow<Int> = animalRepository.getCountBySpecies("Chicken (Layer)")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chickenBroilerCount: StateFlow<Int> = animalRepository.getCountBySpecies("Chicken (Broiler)")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pigCount: StateFlow<Int> = animalRepository.getCountBySpecies("Pig")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val duckCount: StateFlow<Int> = animalRepository.getCountBySpecies("Duck")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Milk Production tracking
    val todayMilkYield: StateFlow<Double?> = flow {
        emit(milkProductionDao.getTotalYieldByDate(
            kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addAnimal(animal: Animal) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                animalRepository.insertAnimal(animal)
                _uiState.update { it.copy(isLoading = false, message = "Animal added successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateAnimal(animal: Animal) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                animalRepository.updateAnimal(animal)
                _uiState.update { it.copy(isLoading = false, message = "Animal updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                animalRepository.deleteAnimal(animal)
                _uiState.update { it.copy(isLoading = false, message = "Animal deleted successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateAnimalStatus(id: Long, status: String) {
        viewModelScope.launch {
            try {
                animalRepository.updateStatus(id, status)
                _uiState.update { it.copy(message = "Status updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    // Health Record methods
    fun getHealthRecordsByAnimal(animalId: Long): Flow<List<HealthRecord>> {
        return healthRecordRepository.getRecordsByAnimalId(animalId)
    }

    fun addHealthRecord(record: HealthRecord) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                healthRecordRepository.insertHealthRecord(record)
                _uiState.update { it.copy(isLoading = false, message = "Health record added successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateHealthRecord(record: HealthRecord) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                healthRecordRepository.updateHealthRecord(record)
                _uiState.update { it.copy(isLoading = false, message = "Health record updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteHealthRecord(record: HealthRecord) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                healthRecordRepository.deleteHealthRecord(record)
                _uiState.update { it.copy(isLoading = false, message = "Health record deleted successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Reproduction Record methods
    fun getReproductionRecordsByDam(damId: Long): Flow<List<ReproductionRecord>> {
        return reproductionDao.getRecordsByDamId(damId)
    }

    fun addReproductionRecord(record: ReproductionRecord) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                reproductionDao.insert(record.copy(isSynced = false))
                _uiState.update { it.copy(isLoading = false, message = "Reproduction record added successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateReproductionRecord(record: ReproductionRecord) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                reproductionDao.update(record.copy(isSynced = false))
                _uiState.update { it.copy(isLoading = false, message = "Reproduction record updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteReproductionRecord(record: ReproductionRecord) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                reproductionDao.delete(record)
                _uiState.update { it.copy(isLoading = false, message = "Reproduction record deleted successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Milk Production methods
    fun getMilkRecordsByAnimal(animalId: Long): Flow<List<MilkProduction>> {
        return milkProductionDao.getRecordsByAnimalId(animalId)
    }

    fun addMilkRecord(record: MilkProduction) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                milkProductionDao.insert(record.copy(isSynced = false))
                _uiState.update { it.copy(isLoading = false, message = "Milk record added successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateMilkRecord(record: MilkProduction) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                milkProductionDao.update(record.copy(isSynced = false))
                _uiState.update { it.copy(isLoading = false, message = "Milk record updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteMilkRecord(record: MilkProduction) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                milkProductionDao.delete(record)
                _uiState.update { it.copy(isLoading = false, message = "Milk record deleted successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Weight Entry methods
    fun getWeightEntriesByAnimal(animalId: Long): Flow<List<WeightEntry>> {
        return weightDao.getWeightEntriesByAnimalId(animalId)
    }

    fun addWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                weightDao.insert(entry.copy(isSynced = false))
                _uiState.update { it.copy(isLoading = false, message = "Weight entry added successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                weightDao.update(entry.copy(isSynced = false))
                _uiState.update { it.copy(isLoading = false, message = "Weight entry updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                weightDao.delete(entry)
                _uiState.update { it.copy(isLoading = false, message = "Weight entry deleted successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class LivestockUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
