package com.example.stockmate.ui.screens.dish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.dtos.IngredientUiModel
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.validationUtil.formErrorsSingleForm
import com.example.stockmate.ui.GenericSmallErrorsList
import com.example.stockmate.ui.components.form.FormTextField
import com.example.stockmate.ui.viewmodels.dish.DishFormViewModel

@Composable
fun IngredientCard(
    uiModel: IngredientUiModel,
    isEditable: Boolean = false,
    onAmountChange: (String) -> Unit = {},
    onProductSelectClick: () -> Unit = {},
    onMultiplierSelectClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
    validationErrors: formErrorsSingleForm<DishFormViewModel.AddIngredientFormField> = emptyMap()
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isEditable) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ingredient details", style = MaterialTheme.typography.labelMedium)
                    IconButton(onClick = onRemoveClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier= Modifier.weight(1f)
                    ) {
                        OutlinedButton(
                            onClick = onProductSelectClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiModel.productName.isEmpty()) "Select a product" else uiModel.productName)
                        }
                        if (validationErrors.containsKey(DishFormViewModel.AddIngredientFormField.PRODUCT)) {
                            GenericSmallErrorsList(
                                errorMessages = validationErrors[DishFormViewModel.AddIngredientFormField.PRODUCT] ?: emptyList(),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    val isProductSelected = uiModel.productName.isNotEmpty()
                    Column(
                        modifier= Modifier.weight(1f)
                    ) {
                        OutlinedButton(
                            onClick = onMultiplierSelectClick,
                            enabled = isProductSelected,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiModel.multiplierName.isEmpty()) "Select a multiplier" else uiModel.multiplierName)
                        }
                        if (validationErrors.containsKey(DishFormViewModel.AddIngredientFormField.MULTIPLIER)) {
                            GenericSmallErrorsList(
                                errorMessages = validationErrors[DishFormViewModel.AddIngredientFormField.MULTIPLIER] ?: emptyList(),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormTextField(
                    value = uiModel.amountInput,
                    onValueChange = onAmountChange,
                    label = "Amount",
                    modifier = Modifier.fillMaxWidth(),
                    errorMessages = validationErrors[DishFormViewModel.AddIngredientFormField.AMOUNT] ?: emptyList()
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = uiModel.productName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = uiModel.amountText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
           }
        }
    }
}