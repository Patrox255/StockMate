package com.example.stockmate.data.prediction.LinearRegression

import com.example.stockmate.data.prediction.DatasetConverter
import com.example.stockmate.data.prediction.math.DataFrame
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinearRegressionTrainer @Inject constructor() {
    fun train(df: DataFrame): LinearRegressionModel {
        val x = df.column(DatasetConverter.DAYS_COLUMN).values
        val y = df.column(DatasetConverter.CONSUMED_COLUMN).values
        val meanX = x.average()
        val meanY = y.average()

        var numerator = 0.0
        var denominator = 0.0
        for (i in x.indices) {
            numerator += (x[i] - meanX) * (y[i] - meanY)
            denominator += (x[i] - meanX) * (x[i] - meanX)
        }

        val slope =
            if (denominator == 0.0) 0.0
            else numerator / denominator
        val intercept = meanY - slope * meanX
        return LinearRegressionModel(slope, intercept)
    }
}