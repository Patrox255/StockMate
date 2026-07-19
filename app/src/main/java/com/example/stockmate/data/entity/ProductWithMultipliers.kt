package com.example.stockmate.data.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class ProductWithMultipliers (
    @Embedded
    val product: Product,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["productId"],
        entity = ProductMultiplier::class
    )
    val multipliers: List<ProductMultiplier>
)