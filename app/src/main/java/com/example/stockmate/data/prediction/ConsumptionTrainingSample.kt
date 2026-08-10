package com.example.stockmate.data.prediction

data class ConsumptionTrainingSample(
    val features: ConsumptionFeatures,
    val consumed: Double,
)

data class ConsumptionFeatures(
    val day: Double,

    // Fields utilized by the Random Forest model for more complex consumption patterns
    val dayOfWeek: Double,
    val stockAtDayStart: Double,
    val previousConsumption: Double,
    val averageConsumption7d: Double,
    val averageConsumption14d: Double,
    val daysSinceLastChange: Double
)