package com.example.stockmate.data.prediction

import com.example.stockmate.data.prediction.LinearRegression.LinearRegressionTrainer
import com.example.stockmate.data.prediction.RandomForest.RandomForestTrainer
import javax.inject.Inject
import javax.inject.Singleton

enum class PredictionModelType {
    CONSUMPTION_LINEAR_REGRESSION,
    CONSUMPTION_RANDOM_FOREST
}

@Singleton
class PredictorFactory @Inject constructor(
    private val datasetConverter: DatasetConverter,
    private val linearRegressionTrainer: LinearRegressionTrainer,
    private val randomForestTrainer: RandomForestTrainer
) {
    fun create(type: PredictionModelType): ConsumptionPredictor =
        when(type) {
            PredictionModelType.CONSUMPTION_LINEAR_REGRESSION ->
                GenericConsumptionPredictor(
                    trainer = linearRegressionTrainer,
                    datasetConverter = datasetConverter
                )
            PredictionModelType.CONSUMPTION_RANDOM_FOREST ->
                GenericConsumptionPredictor(
                    trainer = randomForestTrainer,
                    datasetConverter = datasetConverter
                )
        }
}