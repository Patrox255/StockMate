package com.example.stockmate.data.prediction

import com.example.stockmate.data.prediction.math.DataFrame
import com.example.stockmate.data.prediction.math.Vector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatasetConverter @Inject constructor() {
    companion object {
        const val DAYS_COLUMN = "day"
        const val CONSUMED_COLUMN = "consumed"
        const val DAY_OF_WEEK_PREFIX = "dayOfWeek_"
        const val STOCK_AT_DAY_START_COLUMN = "stockAtDayStart"
        const val PREVIOUS_CONSUMPTION_COLUMN = "previousConsumption"
        const val AVERAGE_CONSUMPTION_7D_COLUMN = "averageConsumption7d"
        const val AVERAGE_CONSUMPTION_14D_COLUMN = "averageConsumption14d"
        const val DAYS_SINCE_LAST_CHANGE_COLUMN = "daysSinceLastChange"
    }

    fun convert(samples: List<ConsumptionTrainingSample>): DataFrame {
        val size = samples.size
        val columns: MutableMap<String, Vector> = mutableMapOf(
            DAYS_COLUMN to Vector(DoubleArray(size) { samples[it].features.day }),
            CONSUMED_COLUMN to Vector(DoubleArray(size) { samples[it].consumed }),
            STOCK_AT_DAY_START_COLUMN to Vector(DoubleArray(size) { samples[it].features.stockAtDayStart }),
            PREVIOUS_CONSUMPTION_COLUMN to Vector(DoubleArray(size) { samples[it].features.previousConsumption }),
            AVERAGE_CONSUMPTION_7D_COLUMN to Vector(DoubleArray(size) { samples[it].features.averageConsumption7d }),
            AVERAGE_CONSUMPTION_14D_COLUMN to Vector(DoubleArray(size) { samples[it].features.averageConsumption14d }),
            DAYS_SINCE_LAST_CHANGE_COLUMN to Vector(DoubleArray(size) { samples[it].features.daysSinceLastChange })
        )

        // One-Hot Encoding for dayOfWeek
        for (dayIndex in 1..7) {
            val oneHotCol = DoubleArray(size) {i ->
                if (samples[i].features.dayOfWeek == dayIndex.toDouble()) 1.0 else 0.0
            }
            columns["$DAY_OF_WEEK_PREFIX$dayIndex"] = Vector(oneHotCol)
        }

        return DataFrame(columns)
    }

    // This is utilized to convert a single ConsumptionFeatures object into a map of feature names
    // to their corresponding values, which can be used for prediction.
    fun extractFeaturesMap(features: ConsumptionFeatures): Map<String, Double> {
        val map = mutableMapOf(
            DAYS_COLUMN to features.day,
            STOCK_AT_DAY_START_COLUMN to features.stockAtDayStart,
            PREVIOUS_CONSUMPTION_COLUMN to features.previousConsumption,
            AVERAGE_CONSUMPTION_7D_COLUMN to features.averageConsumption7d,
            AVERAGE_CONSUMPTION_14D_COLUMN to features.averageConsumption14d,
            DAYS_SINCE_LAST_CHANGE_COLUMN to features.daysSinceLastChange
        )
        for (dayIndex in 1..7) {
            map["$DAY_OF_WEEK_PREFIX$dayIndex"] = if (features.dayOfWeek == dayIndex.toDouble()) 1.0 else 0.0
        }
        return map
    }
}
