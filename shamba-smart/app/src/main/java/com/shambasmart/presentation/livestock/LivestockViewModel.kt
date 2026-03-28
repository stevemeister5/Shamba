package com.shambasmart.presentation.livestock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.domain.repository.AnimalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LivestockViewModel @Inject constructor(
    private val animalRepository: AnimalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LivestockUiState())
    val uiState: StateFlow<LivestockUiState> = _uiState.asStateFlow()

    val allAnimals: StateFlow<List<Animal>> = animalRepository.getAllActiveAnimals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val herdSize: StateFlow<Int> = animalRepository.getActiveAnimalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goatCount: StateFlow<Int> = animalRepository.getCountBySpecies("goat")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sheepCount: StateFlow<Int> = animalRepository.getCountBySpecies("sheep")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // TODO: Implement milk production tracking with repository
    val todayMilkYield: StateFlow<Double?> = flow<Double?> { emit(0.0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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
}

data class LivestockUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)