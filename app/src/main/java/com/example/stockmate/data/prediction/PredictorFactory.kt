package com.example.stockmate.data.prediction

import com.example.stockmate.data.prediction.LinearRegression.LinearRegressionPredictor
import com.example.stockmate.data.prediction.LinearRegression.LinearRegressionTrainer
import javax.inject.Inject
import javax.inject.Singleton

enum class PredictionModelType {
    CONSUMPTION_SMILE_LINEAR_REGRESSION
}

@Singleton
class PredictorFactory @Inject constructor(
    private val datasetConverter: DatasetConverter,
    private val linearRegressionTrainer: LinearRegressionTrainer
) {
    fun create(type: PredictionModelType): ConsumptionPredictor =
        when(type) {
            PredictionModelType.CONSUMPTION_SMILE_LINEAR_REGRESSION ->
                LinearRegressionPredictor(
                    datasetConverter = datasetConverter,
                    trainer = linearRegressionTrainer
                )
        }
}