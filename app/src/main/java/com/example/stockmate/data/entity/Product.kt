package com.example.stockmate.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String,
    val currentStock: Float,
    val targetStock: Float,
    val packageSize: Float,

    val targetDays: Int? = null,
    val dailyConsumptionRate: Float? = null
)
