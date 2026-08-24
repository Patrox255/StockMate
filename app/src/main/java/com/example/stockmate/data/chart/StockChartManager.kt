package com.example.stockmate.data.chart

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.StockLog
import com.example.stockmate.data.repository.ProductRepository
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.math.absoluteValue
import kotlin.random.Random

enum class StockChartProductsFilterMode {
    INCLUDE,
    EXCLUDE
}

data class StockChartManagerSettings(
    val includedOrExcludedProductsIds: List<Long> = emptyList(),
    val filterMode: StockChartProductsFilterMode = StockChartProductsFilterMode.EXCLUDE,
    val allowedChangedReasons: List<ChangeReason> = ChangeReason.entries
)

@Singleton
class StockChartManager @Inject constructor(
    private val productRepository: ProductRepository
){
    suspend fun buildChartData(
        recentLogs: List<StockLog>,
        settings: StockChartManagerSettings,
        startTime: Long
    ): List<StockChartLineData> {
        val filteredLogs = recentLogs.filter { log ->
            settings.allowedChangedReasons.contains(log.changeReason)
        }
        val activeProductIds = filteredLogs
            .map { it.productId }
            .distinct()
            .filter {id ->
                if (settings.filterMode == StockChartProductsFilterMode.INCLUDE) {
                    settings.includedOrExcludedProductsIds.contains(id)
                } else {
                    !settings.includedOrExcludedProductsIds.contains(id)
                }
            }
        val activeProducts = productRepository.getProductsByIds(activeProductIds)
        val chartLines = mutableListOf<StockChartLineData>()
        for (product in activeProducts) {
            val productLogs = filteredLogs
                .filter { it.productId == product.id}
                .sortedBy { it.timestamp }
            val points = mutableListOf<StockChartPoint>()

            for (log in productLogs) {
                val stockAfter = log.stockBefore + log.amountChanged
                points.add(StockChartPoint(
                    timestamp = log.timestamp,
                    value = stockAfter,
                    reason = log.changeReason
                ))
            }

            val curStockPoint = StockChartPoint(
                timestamp = System.currentTimeMillis(),
                value = product.currentStock
            )
            if (points.isNotEmpty()) {
                points.add(curStockPoint)
                points.add(StockChartPoint(
                    timestamp = startTime,
                    value = productLogs.first().stockBefore
                ))
            }
            else {
                // If there are no logs for the product that has been selected to be displayed via filters
                // then we will just display a single point at the start time and a single point at
                // the current time with the current stock value
                points.add(StockChartPoint(
                    timestamp = startTime,
                    value = product.currentStock
                ))
                points.add(curStockPoint)
            }

            val sortedPoints = points.sortedBy { it.timestamp }
            chartLines.add(
                StockChartLineData(
                    product = product,
                    points = sortedPoints,
                    color = generateStableColor(product.id),
                    curStockPointTime = curStockPoint.timestamp
                )
            )
        }
        return chartLines
    }

    fun generateStableColor(productId: Long): Color {
        val random = Random(productId)
        val r = random.nextInt(50, 200)
        val g = random.nextInt(50, 200)
        val b = random.nextInt(50, 200)
        return Color(r, g, b)
    }
}