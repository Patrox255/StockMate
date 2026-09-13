package com.example.stockmate.ui.components.dish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.IngredientWithProductAndMultiplier
import com.example.stockmate.data.mappers.toUiModel
import com.example.stockmate.data.util.formatting.toCleanString
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.screens.dish.IngredientCard

@Composable
fun CookDishDialog(
    dishName: String,
    ingredients: List<IngredientWithProductAndMultiplier>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val stockEvaluations = ingredients.map {item ->
        val required = (item.ingredient.amount * item.multiplier.value).toFloat()
        val hasEnough = item.product.currentStock >= required
        Triple(item, required, hasEnough)
    }

    val canCook = stockEvaluations.all {it.third}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Cook $dishName") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (!canCook) {
                    GenericErrorMessage(
                        errorMessage = "You don't have enough ingredients in stock to cook this dish. Check the missing ingredients below:",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "This will deduct the following items from your inventory:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                stockEvaluations.forEach { (item, required, hasEnough) ->
                    val currentStock = item.product.currentStock

                    Column(
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        IngredientCard(
                            uiModel = item.toUiModel(),
                            isEditable = false
                        )

                        val stockText = "In stock: ${currentStock.toCleanString()} ${item.product.unit} / Required: ${required.toCleanString()} ${item.product.unit}"
                        Text(
                            text = stockText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasEnough) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canCook
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}