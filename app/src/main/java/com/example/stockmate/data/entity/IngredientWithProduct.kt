package com.example.stockmate.data.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class IngredientWithProductAndMultiplier(
    @Embedded
    val ingredient: DishIngredient,

    @Relation(
        parentColumns = ["productId"],
        entityColumns = ["id"],
        entity = Product::class
    )
    val product: Product,
    @Relation(
        parentColumns = ["multiplierId"],
        entityColumns = ["id"],
        entity = ProductMultiplier::class
    )
    val multiplier: ProductMultiplier
)
