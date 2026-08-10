package com.example.stockmate.data.prediction

import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.ChangeReason
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

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
    private val appSettingsRepository: SettingsRepository,
) {
    private val predictionCache = ConcurrentHashMap<PredictionCacheKey, PredictionCacheValue>()

    suspend fun retrain(productId: Long): ConsumptionPredictor? {
        val logs = stockLogDao.getLogsForProduct(productId)
        val samples = datasetBuilder.buildDatasetBasedOnSingleProductLogs(logs)
        if (samples.size < 2) return null

        val currentModel = appSettingsRepository.settings.first().predictionSelectedModel
        val predictor = predictorFactory.create(currentModel)
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
        val tomorrow = today.plusDays(1)
        val nextDayIndex = ChronoUnit.DAYS.between(firstLogDate, tomorrow).toDouble()
        val tomorrowDayOfWeek = tomorrow.dayOfWeek.value.toDouble()

        val consumedLogs = logs.filter { it.changeReason == ChangeReason.CONSUMED }
        val last7DaysLogs = consumedLogs.filter {
            it.timestamp >= System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        }
        val avg7d = if (last7DaysLogs.isNotEmpty()) {
            last7DaysLogs.sumOf { abs(it.amountChanged.toDouble()) } / 7.0
        } else {
            0.0
        }
        val last14DaysLogs = consumedLogs.filter {
            it.timestamp >= System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)
        }
        val avg14d = if (last14DaysLogs.isNotEmpty()) {
            last14DaysLogs.sumOf { abs(it.amountChanged.toDouble()) } / 14.0
        } else {
            0.0
         }
        val lastChangeLog = logs.maxByOrNull { it.timestamp }
        val daysSinceLastChange = if (lastChangeLog != null) {
            val changeDate = Instant.ofEpochMilli(lastChangeLog.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.DAYS.between(changeDate, today).toDouble()
        } else {
            0.0
        }

        val consumptionFeatures = ConsumptionFeatures(
            day = nextDayIndex,
            dayOfWeek = tomorrowDayOfWeek,
            stockAtDayStart = product.currentStock.toDouble(),
            previousConsumption =
                consumedLogs.maxByOrNull { it.timestamp }?.amountChanged ?.let { abs(it.toDouble()) } ?: 0.0,
            averageConsumption7d = avg7d,
            averageConsumption14d = avg14d,
            daysSinceLastChange = daysSinceLastChange
        )

        var predictedConsumption = predictor.predict(consumptionFeatures)
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