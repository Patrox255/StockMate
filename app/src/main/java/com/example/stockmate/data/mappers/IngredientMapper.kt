package com.example.stockmate.data.mappers

import com.example.stockmate.data.dtos.IngredientUiModel
import com.example.stockmate.data.entity.DishWithIngredients
import com.example.stockmate.data.entity.IngredientWithProductAndMultiplier
import com.example.stockmate.ui.viewmodels.dish.DraftDish
import com.example.stockmate.ui.viewmodels.dish.DraftIngredient

fun DraftIngredient.toUiModel(): IngredientUiModel {
    return IngredientUiModel(
        localId = this.localId,
        productName = this.product?.name ?: "",
        multiplierName = this.multiplier?.name ?: "",
        amountText = if (this.multiplier != null) {
            "${this.amount} x ${this.multiplier.name} (${this.multiplier.value * this.amount} ${this.multiplier.name})"
        } else {
            "${this.amount} x Unknown multiplier"
        },
        amountInput = this.amount.toString()
    )
}

fun IngredientWithProductAndMultiplier.toUiModel(): IngredientUiModel {
    return IngredientUiModel(
        // random UUID for localId
        productName = this.product.name ?: "Unknown product",
        multiplierName = this.multiplier.name ?: "Unknown multiplier",
        amountText = "${this.ingredient.amount} x ${this.multiplier.name} (${this.multiplier.value * this.ingredient.amount} ${this.multiplier.name})"
    )
}

fun DishWithIngredients.toDraftDish(): DraftDish {
    return DraftDish(
        name = this.dish.name,
        description = this.dish.description ?: "",
        ingredients = this.ingredients.map { ingredientWithProductAndMultiplier ->
            DraftIngredient(
                databaseId = ingredientWithProductAndMultiplier.ingredient.id,
                product = ingredientWithProductAndMultiplier.product,
                multiplier = ingredientWithProductAndMultiplier.multiplier,
                amount = ingredientWithProductAndMultiplier.ingredient.amount
            )
        }
    )
}