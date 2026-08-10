package com.example.stockmate.data.prediction

import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.StockLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

fun List<StockLog>.getFirstLogDate(): LocalDate? {
    if (this.isEmpty())
        return null
    return Instant.ofEpochMilli(this.minOf { it.timestamp })
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun List<StockLog>.getLastLogDate(): LocalDate? {
    return Instant.ofEpochMilli(this.maxOf { it.timestamp })
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}


@Singleton
class DatasetBuilder @Inject constructor() {
    // We rely here on logs related to a single product, as we will build models for each product separately
    // therefore upon receiving a new log it can effectively be applied to retrain without much effort
    fun buildDatasetBasedOnSingleProductLogs(logs: List<StockLog>): List<ConsumptionTrainingSample> {
        val sortedLogs = logs.sortedBy { it.timestamp }
        // All entries have a day field which is the number of days since the first log entry
        val firstLogDate = sortedLogs.getFirstLogDate() ?: return emptyList()
        val lastLogDate = sortedLogs.getLastLogDate() ?: return emptyList()

        val logsWithDayIndex = sortedLogs.map { log ->
            val date = Instant.ofEpochMilli(log.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val dayIndex = ChronoUnit.DAYS.between(firstLogDate, date)
            log to dayIndex
        }
        val consumptionByDay: Map<Long, Double> = logsWithDayIndex
            .filter { it.first.changeReason == ChangeReason.CONSUMED }
            .groupBy {it.second}
            .mapValues { (_, logsForDay) ->
                logsForDay.sumOf { (log, _) ->
                    abs(log.amountChanged.toDouble())
                }
            }
        val stockAtDayStartByDay: Map<Long, Double> =
            logsWithDayIndex
                .groupBy { it.second }
                .mapValues { (_, logsForDay) ->
                    logsForDay
                        .minBy { (log, _) -> log.timestamp }
                        .first
                        .stockBefore
                        .toDouble()
                }
        var lastChangeDay: Long? = null
        var previousConsumption = 0.0
        val totalDays = ChronoUnit.DAYS.between(firstLogDate, lastLogDate).toLong()
        val consumptionTrainingSamples = mutableListOf<ConsumptionTrainingSample>()
        for (dayIndex in 0..totalDays) {
            val currentDate = firstLogDate.plusDays(dayIndex)
            val consumed = consumptionByDay[dayIndex] ?: 0.0
            val stockAtDayStart = stockAtDayStartByDay[dayIndex] ?:
                findLastKnownStock(logsWithDayIndex, dayIndex)
            val averageConsumption7d = calculateAverageConsumption(
                consumptionByDay,
                dayIndex,
                7
            )
            val averageConsumption14d = calculateAverageConsumption(
                consumptionByDay,
                dayIndex,
                14
            )
            val daysSinceLastChange =
                if (lastChangeDay == null) {
                    0.0
                } else {
                    (dayIndex - lastChangeDay).toDouble()
                }
            val dayOfWeek = currentDate.dayOfWeek.value.toDouble()

            val consumptionFeatures = ConsumptionFeatures(
                day = dayIndex.toDouble(),
                dayOfWeek = dayOfWeek,
                stockAtDayStart = stockAtDayStart,
                previousConsumption = previousConsumption,
                averageConsumption7d = averageConsumption7d,
                averageConsumption14d = averageConsumption14d,
                daysSinceLastChange = daysSinceLastChange
            )
            consumptionTrainingSamples += ConsumptionTrainingSample(
                features = consumptionFeatures,
                consumed = consumed
            )

            previousConsumption = consumed
            if (logsWithDayIndex.any { it.second == dayIndex}) {
                lastChangeDay = dayIndex
            }
        }

        return consumptionTrainingSamples
    }

    private fun calculateAverageConsumption(
        consumptionByDay: Map<Long, Double>,
        currentDay: Long,
        windowSize: Long
    ): Double {
        if (currentDay <= 0) {
            return 0.0
        }
        val startDay = maxOf(
            0L,
            currentDay - windowSize
        )
        return (startDay until currentDay)
            .map { day ->
                consumptionByDay[day] ?: 0.0
            }
            .average()
    }


    private fun findLastKnownStock(
        logsWithDayIndex: List<Pair<StockLog, Long>>,
        dayIndex: Long
    ): Double {
        val previousLog = logsWithDayIndex
            .filter { it.second < dayIndex}
            .maxByOrNull { it.first.timestamp }
        return previousLog?.first?.let {log ->
            log.stockBefore.toDouble() + log.amountChanged.toDouble()
        } ?: 0.0
    }
}