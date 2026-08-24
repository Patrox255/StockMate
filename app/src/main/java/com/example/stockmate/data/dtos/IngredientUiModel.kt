package com.example.stockmate.data.dtos

import java.util.UUID

data class IngredientUiModel(
    val localId: String = UUID.randomUUID().toString(),
    val productName: String,
    val multiplierName: String,
    val amountText: String,
    val amountInput: String = "",
)
