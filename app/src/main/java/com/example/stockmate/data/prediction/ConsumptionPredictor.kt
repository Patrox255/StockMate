package com.example.stockmate.data.prediction

import com.example.stockmate.data.prediction.math.DataFrame

interface ConsumptionPredictor {
    suspend fun train(samples: List<ConsumptionTrainingSample>)
    fun predict(features: ConsumptionFeatures): Double
}
interface ConsumptionModel {
    fun predict(features: Map<String, Double>): Double
}
interface ConsumptionTrainer {
    suspend fun train(df: DataFrame): ConsumptionModel
}