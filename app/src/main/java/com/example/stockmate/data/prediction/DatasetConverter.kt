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
    }

    fun convert(samples: List<ConsumptionTrainingSample>): DataFrame {
        return DataFrame(
            mapOf(
                DAYS_COLUMN to Vector(samples.map { it.day }.toDoubleArray()),
                CONSUMED_COLUMN to Vector(samples.map { it.consumed }.toDoubleArray())
            )
        )
    }
}
