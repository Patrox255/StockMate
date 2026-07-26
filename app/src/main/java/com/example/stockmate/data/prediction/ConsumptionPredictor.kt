package com.example.stockmate.data.prediction

interface ConsumptionPredictor {
    fun train(samples: List<ConsumptionTrainingSample>)
    fun predict(day: Double): Double
}