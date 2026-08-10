package com.example.stockmate.data.prediction

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.stockmate.data.dtos.AppSettings
import com.example.stockmate.data.prediction.RandomForest.RandomForestModelSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val PREDICTION_SETTINGS_PREFIX = "prediction_settings_"
        val NUM_TREES = intPreferencesKey("${PREDICTION_SETTINGS_PREFIX}num_trees")
        val MAX_DEPTH = intPreferencesKey("${PREDICTION_SETTINGS_PREFIX}max_depth")
        val MIN_SAMPLES_SPLIT = intPreferencesKey("${PREDICTION_SETTINGS_PREFIX}min_samples_split")
        val FEATURES_SUBSET_SIZE_DIVIDER = intPreferencesKey("${PREDICTION_SETTINGS_PREFIX}features_subset_size_divider")
        val CONSUMPTION_PREDICTION_SELECTED_MODEL = intPreferencesKey("${PREDICTION_SETTINGS_PREFIX}consumption_prediction_selected_model")
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch {e ->
            if (e is IOException) {
                e.printStackTrace()
                emit(emptyPreferences())
            } else throw e
        }
        .map {prefs ->
            val defaults = AppSettings()

            AppSettings(
                predictionSelectedModel = prefs[Keys.CONSUMPTION_PREDICTION_SELECTED_MODEL]?.let {
                    PredictionModelType.entries.getOrNull(it)
                } ?: defaults.predictionSelectedModel,

                randomForestSettings = RandomForestModelSettings(
                    numTrees = prefs[Keys.NUM_TREES] ?: defaults.randomForestSettings.numTrees,
                    maxDepth = prefs[Keys.MAX_DEPTH] ?: defaults.randomForestSettings.maxDepth,
                    minSamplesSplit = prefs[Keys.MIN_SAMPLES_SPLIT]
                        ?: defaults.randomForestSettings.minSamplesSplit,
                    featuresSubsetSizeDivider = prefs[Keys.FEATURES_SUBSET_SIZE_DIVIDER]
                        ?: defaults.randomForestSettings.featuresSubsetSizeDivider
                )
            )
        }

    suspend fun updateSettings(newSettings: AppSettings): Result<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[Keys.CONSUMPTION_PREDICTION_SELECTED_MODEL] = newSettings.predictionSelectedModel.ordinal
                prefs[Keys.NUM_TREES] = newSettings.randomForestSettings.numTrees
                prefs[Keys.MAX_DEPTH] = newSettings.randomForestSettings.maxDepth
                prefs[Keys.MIN_SAMPLES_SPLIT] = newSettings.randomForestSettings.minSamplesSplit
                prefs[Keys.FEATURES_SUBSET_SIZE_DIVIDER] = newSettings.randomForestSettings.featuresSubsetSizeDivider
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}