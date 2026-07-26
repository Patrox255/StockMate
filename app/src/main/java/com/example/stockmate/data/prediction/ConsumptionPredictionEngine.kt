package com.example.stockmate.data.prediction

import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.StockLogDao
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class PredictionCacheKey(
    val productId: Long,
)

data class PredictionCacheValue(
    val predictor: ConsumptionPredictor,
)

@Singleton
class ConsumptionPredictionEngine @Inject constructor(
    private val stockLogDao: StockLogDao,
    private val productDao: ProductDao,
    private val datasetBuilder: DatasetBuilder,
    private val predictorFactory: PredictorFactory,
    private val predictionSettings: PredictionSettings
) {
    private val predictionCache = ConcurrentHashMap<PredictionCacheKey, PredictionCacheValue>()

    suspend fun retrain(productId: Long): ConsumptionPredictor? {
        val logs = stockLogDao.getLogsForProduct(productId)
        val samples = datasetBuilder.buildDatasetBasedOnSingleProductLogs(logs)
        if (samples.size < 2) return null

        val predictor = predictorFactory.create(predictionSettings.currentModel)
        predictor.train(samples)
        predictionCache[PredictionCacheKey(productId)] = PredictionCacheValue(
            predictor = predictor,
        )
        return predictor
    }

    suspend fun predict(productId: Long): ConsumptionPredictionResult? {
        var predictor = predictionCache[PredictionCacheKey(productId)]?.predictor
            ?: retrain(productId)
            ?: return null

        val product = productDao.getProductById(productId) ?: return null
        val logs = stockLogDao.getLogsForProduct(productId)
        if (logs.isEmpty())
            return null

        val firstLogDate = logs.getFirstLogDate() ?: return null
        val today = LocalDate.now()
        val nextDayIndex = ChronoUnit.DAYS.between(firstLogDate, today).toDouble() + 1.0

        var predictedConsumption = predictor.predict(nextDayIndex)
        if (predictedConsumption < 0.0) {
            predictedConsumption = 0.0
        }
        val daysUntilEmpty = if (predictedConsumption > 0) {
            product.currentStock / predictedConsumption
        } else {
            Double.POSITIVE_INFINITY
        }
        return ConsumptionPredictionResult(
            predictedDailyConsumption = predictedConsumption,
            predictedDaysUntilEmpty = daysUntilEmpty,
            modelAccuracy = null
        )
    }

    fun clearCache() {
        predictionCache.clear()
    }

    fun clearCacheForProduct(productId: Long) {
        predictionCache.remove(PredictionCacheKey(productId))
    }
}