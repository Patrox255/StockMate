package com.example.stockmate.data.prediction.LinearRegression

import com.example.stockmate.data.prediction.ConsumptionModel
import com.example.stockmate.data.prediction.DatasetConverter

data class LinearRegressionModel(
    val slope: Double,
    val intercept: Double
) : ConsumptionModel {
    override fun predict(features: Map<String, Double>): Double {
        return slope * features[DatasetConverter.DAYS_COLUMN]!! + intercept
    }
}
