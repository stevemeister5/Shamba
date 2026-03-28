package com.shambasmart.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.preferences.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val isLoading: Boolean = false,
    val permissionsGranted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val isOnboardingCompleted: Flow<Boolean> = onboardingPreferences.isOnboardingCompleted

    fun nextPage() {
        _uiState.update { 
            if (it.currentPage < it.totalPages - 1) {
                it.copy(currentPage = it.currentPage + 1)
            } else it
        }
    }

    fun previousPage() {
        _uiState.update { 
            if (it.currentPage > 0) {
                it.copy(currentPage = it.currentPage - 1)
            } else it
        }
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            onboardingPreferences.completeOnboarding()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun setPermissionsGranted(granted: Boolean) {
        viewModelScope.launch {
            onboardingPreferences.setPermissionsGranted(granted)
            _uiState.update { it.copy(permissionsGranted = granted) }
        }
    }
}