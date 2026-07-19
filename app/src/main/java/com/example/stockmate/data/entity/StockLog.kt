package com.example.stockmate.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "stock_logs")
data class StockLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    // In milliseconds
    val timestamp: Long,
    val amountChanged: Float,
    val changeReason: ChangeReason
)

enum class ChangeReason(val displayName: String) {
    RESTOCKED("Restocked"),
    CONSUMED("Consumed"),
    WASTED("Wasted"),
    ADJUSTED("Adjusted")
}
