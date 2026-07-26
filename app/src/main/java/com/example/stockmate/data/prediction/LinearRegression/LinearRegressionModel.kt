package com.example.stockmate.data.prediction.LinearRegression

data class LinearRegressionModel(
    val slope: Double,
    val intercept: Double
) {
    fun predict(x: Double): Double {
        return slope * x + intercept
    }
}
