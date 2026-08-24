package com.example.stockmate.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "product_multipliers",
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
data class ProductMultiplier (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val name: String,
    val value: Float,
    val sortOrder: Int
)