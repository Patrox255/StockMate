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
    return Instant.ofEpochMilli(this.first().timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun List<StockLog>.getLastLogDate(): LocalDate? {
    return Instant.ofEpochMilli(this.last().timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}


@Singleton
class DatasetBuilder @Inject constructor() {
    // We rely here on logs related to a single product, as we will build models for each product separately
    // therefore upon receiving a new log it can effectively be applied to retrain without much effort
    fun buildDatasetBasedOnSingleProductLogs(logs: List<StockLog>): List<ConsumptionTrainingSample> {
        val consumedLogs = logs.filter { it.changeReason == ChangeReason.CONSUMED }
        if (consumedLogs.isEmpty()) {
            return emptyList()
        }

        val sortedLogs = consumedLogs.sortedBy { it.timestamp }
        // All entries have a day field which is the number of days since the first log entry
        val firstLogDate = sortedLogs.getFirstLogDate() ?: return emptyList()
        val lastLogDate = sortedLogs.getLastLogDate() ?: return emptyList()

        val consumptionMap = sortedLogs.groupBy { log ->
            val logDate = Instant.ofEpochMilli(log.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            ChronoUnit.DAYS.between(firstLogDate, logDate).toLong()
        }.mapValues { (_, logsForDay) ->
            logsForDay.sumOf { abs(it.amountChanged.toDouble()) }
        }

        val totalDays = ChronoUnit.DAYS.between(firstLogDate, lastLogDate).toLong()
        val consumptionTrainingSamples = mutableListOf<ConsumptionTrainingSample>()
        for (dayIndex in 0..totalDays) {
            val consumed = consumptionMap[dayIndex] ?: 0.0
            consumptionTrainingSamples.add(ConsumptionTrainingSample(day = dayIndex.toDouble(), consumed = consumed))
        }

        return consumptionTrainingSamples
    }
}