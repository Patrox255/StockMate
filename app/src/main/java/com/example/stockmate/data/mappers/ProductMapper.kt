package com.example.stockmate.data.mappers

import com.example.stockmate.data.dtos.AddProductFormState
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier


fun AddProductFormState.toProduct(
    id: Long = 0L,
    currentStock: Float = 0f
): Product {
    return Product(
        id = id,
        name = name,
        unit = unit,
        currentStock = currentStock,
        targetStock = targetStock.toFloatOrNull() ?: 0f,
        imageUrl = imagePath
    )
}

fun MultiplierFormState.toMultiplier(
    productId: Long,
    sortOrder: Int
): ProductMultiplier {
    return ProductMultiplier(
        id = databaseId ?: 0L,
        productId = productId,
        name = name,
        value = value.toFloat(),
        sortOrder = sortOrder
    )
}

fun List<MultiplierFormState>.toMultipliers(
    productId: Long
): List<ProductMultiplier> {
    return this.mapIndexed { index, multiplierFormState ->
        multiplierFormState.toMultiplier(productId, index)
    }
}