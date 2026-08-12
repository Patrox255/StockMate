package com.example.stockmate.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "stock_logs",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class StockLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    // In milliseconds
    val timestamp: Long,
    val amountChanged: Float,
    val changeReason: ChangeReason,
    val stockBefore: Float
)

enum class ChangeReason(val displayName: String) {
    RESTOCKED("Restocked"),
    CONSUMED("Consumed"),
    WASTED("Wasted"),
    ADJUSTED("Adjusted");

    companion object {
        val INCREASING_REASONS = listOf(
            RESTOCKED,
            ADJUSTED
        )
        val DECREASING_REASONS = listOf(
            CONSUMED,
            WASTED,
            ADJUSTED
        )
    }
}
