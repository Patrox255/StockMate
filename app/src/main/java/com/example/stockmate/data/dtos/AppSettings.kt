package com.example.stockmate.data.dtos

import com.example.stockmate.data.prediction.PredictionModelType
import com.example.stockmate.data.prediction.RandomForest.RandomForestModelSettings

data class AppSettings(
    val predictionSelectedModel: PredictionModelType = PredictionModelType.CONSUMPTION_LINEAR_REGRESSION,
    val randomForestSettings: RandomForestModelSettings = RandomForestModelSettings()
)
