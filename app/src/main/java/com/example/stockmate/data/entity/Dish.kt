package com.example.stockmate.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val imagePath: String? = null
)