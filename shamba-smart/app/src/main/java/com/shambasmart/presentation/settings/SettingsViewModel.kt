package com.shambasmart.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.preferences.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load saved settings on initialization
        viewModelScope.launch {
            combine(
                settingsPreferences.selectedLanguage,
                settingsPreferences.userRole,
                settingsPreferences.notificationsEnabled,
                settingsPreferences.farmName,
                settingsPreferences.farmLocation,
                settingsPreferences.farmSize
            ) { language, role, notifications, farmName, farmLocation, farmSize ->
                SettingsUiState(
                    selectedLanguage = language,
                    userRole = role,
                    notificationsEnabled = notifications,
                    farmProfile = FarmProfile(
                        name = farmName,
                        location = farmLocation,
                        size = farmSize
                    )
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedLanguage = language) }
            settingsPreferences.updateLanguage(language)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(notificationsEnabled = enabled) }
            settingsPreferences.updateNotificationsEnabled(enabled)
        }
    }

    fun updateUserRole(role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userRole = role) }
            settingsPreferences.updateUserRole(role)
        }
    }

    fun updateFarmProfile(profile: FarmProfile) {
        viewModelScope.launch {
            _uiState.update { it.copy(farmProfile = profile) }
            settingsPreferences.updateFarmProfile(profile.name, profile.location, profile.size)
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