package com.shambasmart.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedLanguage = language) }
            // TODO: Save to DataStore
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(notificationsEnabled = enabled) }
            // TODO: Save to DataStore
        }
    }

    fun updateUserRole(role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userRole = role) }
            // TODO: Save to DataStore
        }
    }

    fun updateFarmProfile(profile: FarmProfile) {
        viewModelScope.launch {
            _uiState.update { it.copy(farmProfile = profile) }
            // TODO: Save to DataStore
        }
    }
}

data class SettingsUiState(
    val selectedLanguage: String = "English",
    val notificationsEnabled: Boolean = true,
    val userRole: String = "Owner",
    val farmProfile: FarmProfile = FarmProfile(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FarmProfile(
    val name: String = "Shamba Smart Farm",
    val location: String = "Korogwe, Tanga",
    val size: String = "16 acres",
    val ownerContact: String = "",
    val registrationNumber: String = ""
)