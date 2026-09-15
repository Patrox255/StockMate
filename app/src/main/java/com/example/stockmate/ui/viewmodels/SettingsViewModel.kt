package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dtos.AppSettings
import com.example.stockmate.data.prediction.ConsumptionPredictionEngine
import com.example.stockmate.data.prediction.SettingsRepository
import com.example.stockmate.data.seeder.DatabaseSeeder
import com.example.stockmate.data.seeder.SeederAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val predictionEngine: ConsumptionPredictionEngine,
    private val databaseSeeder: DatabaseSeeder
): ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            predictionEngine.clearCache()
            settingsRepository.updateSettings(newSettings)
                .onFailure { error ->
                    _saveError.value = "Failed to save settings. Please try again."
                }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            databaseSeeder.seedDatabase(
                seederAction = SeederAction.CLEAR
            )
        }
    }
    fun seedDatabase() {
        viewModelScope.launch {
            databaseSeeder.seedDatabase(
                seederAction = SeederAction.SEED
            )
        }
    }
}