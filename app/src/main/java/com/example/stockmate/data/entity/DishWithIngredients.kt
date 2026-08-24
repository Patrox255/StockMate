package com.example.stockmate.data.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class DishWithIngredients(
    @Embedded
    val dish: Dish,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["dishId"],
        entity = DishIngredient::class
    )
    val ingredients: List<IngredientWithProductAndMultiplier>
)
