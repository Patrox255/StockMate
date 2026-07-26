package com.example.stockmate.data.prediction.LinearRegression

import com.example.stockmate.data.prediction.ConsumptionPredictor
import com.example.stockmate.data.prediction.ConsumptionTrainingSample
import com.example.stockmate.data.prediction.DatasetConverter

class LinearRegressionPredictor constructor(
    private val datasetConverter: DatasetConverter,
    private val trainer: LinearRegressionTrainer
): ConsumptionPredictor {
    private var model: LinearRegressionModel? = null

    override fun train(samples: List<ConsumptionTrainingSample>) {
        model = trainer.train(datasetConverter.convert(samples))
    }

    override fun predict(day: Double): Double {
        return model?.predict(day) ?: 0.0
    }
}