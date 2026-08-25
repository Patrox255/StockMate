package com.example.stockmate.data.mappers

import com.example.stockmate.data.dtos.IngredientUiModel
import com.example.stockmate.data.entity.Dish
import com.example.stockmate.data.entity.DishIngredient
import com.example.stockmate.data.entity.DishWithIngredients
import com.example.stockmate.data.entity.IngredientWithProductAndMultiplier
import com.example.stockmate.data.util.formatting.toCleanString
import com.example.stockmate.ui.viewmodels.dish.DraftDish
import com.example.stockmate.ui.viewmodels.dish.DraftIngredient

fun DraftIngredient.toUiModel(): IngredientUiModel {
    val amount = this.amountStr.toDoubleOrNull()
    return IngredientUiModel(
        localId = this.localId,
        productName = this.product?.name ?: "",
        multiplierName = this.multiplier?.name ?: "",
        amountText = if (this.multiplier != null && amount != null) {
            "${this.amountStr} x ${this.multiplier.name} (${(this.multiplier.value * amount).toCleanString()} ${this.product!!.unit})"
        } else if (amount == null) {
            "${this.amountStr} x ${this.multiplier?.name ?: "Unknown multiplier"} (Invalid amount)"
        } else {
            "${this.amountStr} x Unknown multiplier"
        },
        amountInput = this.amountStr
    )
}

fun IngredientWithProductAndMultiplier.toUiModel(): IngredientUiModel {
    return IngredientUiModel(
        // random UUID for localId
        productName = this.product.name ?: "Unknown product",
        multiplierName = this.multiplier.name ?: "Unknown multiplier",
        amountText = "${this.ingredient.amount.toCleanString()} x ${this.multiplier.name} (${(this.multiplier.value * this.ingredient.amount).toCleanString()} ${this.product.unit})"
    )
}

fun DishWithIngredients.toDraftDish(): DraftDish {
    return DraftDish(
        name = this.dish.name,
        description = this.dish.description ?: "",
        imagePath = this.dish.imagePath,
        ingredients = this.ingredients.map { ingredientWithProductAndMultiplier ->
            DraftIngredient(
                databaseId = ingredientWithProductAndMultiplier.ingredient.id,
                product = ingredientWithProductAndMultiplier.product,
                multiplier = ingredientWithProductAndMultiplier.multiplier,
                amountStr = ingredientWithProductAndMultiplier.ingredient.amount.toCleanString()
            )
        },

    )
}

fun DraftDish.toDishEntity(dishId: Long?): Dish {
    return Dish(
        id = dishId ?: 0L,
        name = this.name,
        description = this.description.ifBlank { null },
        imagePath = this.imagePath
    )
}

fun List<DraftIngredient>.toIngredients(dishId: Long?): List<DishIngredient> {
    return this.map { draftIngredient ->
        draftIngredient.toEntity(dishId)
    }
}

fun DraftIngredient.toEntity(dishId: Long?): DishIngredient {
    return DishIngredient(
        id = this.databaseId ?: 0L,
        productId = this.product?.id ?: 0L,
        multiplierId = this.multiplier?.id ?: 0L,
        amount = this.amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0,
        dishId = dishId ?: 0L
    )
}