package com.example.stockmate.data.prediction

data class ConsumptionPredictionResult(
    val predictedDailyConsumption: Double,
    val predictedDaysUntilEmpty: Double,
    val modelAccuracy: Double?
)
