package com.shambasmart.presentation.settings

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.ShambaDatabase
import com.shambasmart.data.preferences.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val database: ShambaDatabase,
    @ApplicationContext private val context: Context
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
            ) { values: Array<Any> ->
                val language = values[0] as String
                val role = values[1] as String
                val notifications = values[2] as Boolean
                val farmName = values[3] as String
                val farmLocation = values[4] as String
                val farmSize = values[5] as String
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

    fun exportData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val exportData = JSONObject()
                
                // Export animals
                val animals = database.animalDao().getAllActiveAnimals()
                val animalsArray = JSONArray()
                animals.collect { animalList ->
                    animalList.forEach { animal ->
                        animalsArray.put(JSONObject().apply {
                            put("id", animal.id)
                            put("tagId", animal.tagId)
                            put("species", animal.species)
                            put("breed", animal.breed)
                            put("sex", animal.sex)
                            put("status", animal.status)
                        })
                    }
                }
                exportData.put("animals", animalsArray)
                
                // Export plots
                val plots = database.plotDao().getAllPlots()
                val plotsArray = JSONArray()
                plots.collect { plotList ->
                    plotList.forEach { plot ->
                        plotsArray.put(JSONObject().apply {
                            put("id", plot.id)
                            put("name", plot.name)
                            put("sizeAcres", plot.sizeAcres)
                            put("soilType", plot.soilType)
                        })
                    }
                }
                exportData.put("plots", plotsArray)
                
                // Save to Downloads folder
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "shamba_smart_export_${System.currentTimeMillis()}.json")
                FileOutputStream(file).use { fos ->
                    fos.write(exportData.toString(2).toByteArray())
                }
                
                _uiState.update { it.copy(isLoading = false, message = "Data exported to ${file.absolutePath}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Export failed: ${e.message}") }
            }
        }
    }

    fun backupData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Create backup directory
                val backupDir = File(context.filesDir, "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                
                // Create backup file with timestamp
                val backupFile = File(backupDir, "shamba_backup_${System.currentTimeMillis()}.json")
                val backupData = JSONObject()
                
                // Backup all data from all tables
                // Animals
                val animals = database.animalDao().getAllActiveAnimals()
                val animalsArray = JSONArray()
                animals.collect { animalList ->
                    animalList.forEach { animal ->
                        animalsArray.put(JSONObject().apply {
                            put("tagId", animal.tagId)
                            put("species", animal.species)
                            put("breed", animal.breed)
                            put("sex", animal.sex)
                            put("dateOfBirth", animal.dateOfBirth?.toString())
                            put("status", animal.status)
                            put("notes", animal.notes)
                        })
                    }
                }
                backupData.put("animals", animalsArray)
                
                // Save backup
                FileOutputStream(backupFile).use { fos ->
                    fos.write(backupData.toString(2).toByteArray())
                }
                
                _uiState.update { it.copy(isLoading = false, message = "Backup created: ${backupFile.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Backup failed: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

data class SettingsUiState(
    val selectedLanguage: String = "English",
    val notificationsEnabled: Boolean = true,
    val userRole: String = "Owner",
    val farmProfile: FarmProfile = FarmProfile(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

data class FarmProfile(
    val name: String = "Shamba Smart Farm",
    val location: String = "Korogwe, Tanga",
    val size: String = "16 acres",
    val ownerContact: String = "",
    val registrationNumber: String = ""
)