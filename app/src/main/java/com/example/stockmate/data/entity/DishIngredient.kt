package com.example.stockmate.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "dish_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Dish::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("dishId"),
        Index("productId")
    ]
)
data class DishIngredient (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dishId: Long,
    val productId: Long,
    val amount: Double,
    val multiplierId: Long
)