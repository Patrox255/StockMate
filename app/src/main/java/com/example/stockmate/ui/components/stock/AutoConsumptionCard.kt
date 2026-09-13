package com.example.stockmate.ui.components.stock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.util.formatting.toCleanString
import com.example.stockmate.data.util.formatting.toFloatOrNullWithCommaSupport
import com.example.stockmate.ui.viewmodels.stock.AutoConsumptionItemState
import com.example.stockmate.ui.viewmodels.stock.SuggestionMode

@Composable
fun AutoConsumptionCard(
    state: AutoConsumptionItemState,
    onModeChanged: (SuggestionMode) -> Unit,
    onAmountChanged: (String) -> Unit,
    onToggleSelection: () -> Unit,
    onDishToggled: (Long, String) -> Unit,
    onApplyDishToAll: (String) -> Unit,
) {
    val cardAlpha = if (state.isSelected) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(cardAlpha)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.isSelected,
                        onCheckedChange = { onToggleSelection() }
                    )
                    Text(state.product.name, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "Stock: ${state.product.currentStock.toCleanString()} ${state.product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedMode == SuggestionMode.YESTERDAY,
                    onClick = { onModeChanged(SuggestionMode.YESTERDAY) },
                    label = { Text("Yesterday: ${state.yesterdayAmount.toCleanString()} ${state.product.unit}") },
                    enabled = state.isSelected && state.yesterdayAmount > 0f
                )
                FilterChip(
                    selected = state.selectedMode == SuggestionMode.PREDICTION,
                    onClick = { onModeChanged(SuggestionMode.PREDICTION) },
                    label = { Text("AI prediction: ${state.predictedAmount.toCleanString()} ${state.product.unit}") },
                    enabled = state.isSelected && state.predictedAmount > 0f
                )
                if (state.todayAmount > 0f && state.todayAmount < state.yesterdayAmount) {
                    val equalizeAmount = state.yesterdayAmount - state.todayAmount
                    FilterChip(
                        selected = state.selectedMode == SuggestionMode.EQUALIZE_TO_YESTERDAY,
                        onClick = { onModeChanged(SuggestionMode.EQUALIZE_TO_YESTERDAY) },
                        label = { Text("Equalize to yesterday: ${equalizeAmount.toCleanString()} ${state.product.unit}") },
                        enabled = state.isSelected
                    )
                }
            }

            if (state.yesterdayDishes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yesterday's usage details according to ${state.yesterdayDishes.size} " +
                            "${if (state.yesterdayDishes.size == 1) "dish" else "dishes"}:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.yesterdayDishes.forEach { breakdown ->
                        val isThisDishSelected = state.selectedDishes.contains(breakdown.dishName)

                        FilterChip(
                            selected = isThisDishSelected,
                            onClick = {
                                onDishToggled(state.product.id, breakdown.dishName)
                            },
                            label = {
                                Text("${breakdown.dishName}: ${breakdown.amountConsumed.toCleanString()} ${state.product.unit}")
                            },
                            enabled = state.isSelected
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.recentToggledDish != null
                ) {
                    TextButton(
                        onClick = { state.recentToggledDish?.let { onApplyDishToAll(it) } },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome,
                            contentDescription = "Apply to all related products",
                            modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync ${state.recentToggledDish} contributions to other related products")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.amountInput,
                onValueChange = onAmountChanged,
                label = { Text("Amount to consume (${state.product.unit})") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isSelected,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (state.isSelected) {
                val currentInput = state.amountInput.toFloatOrNullWithCommaSupport()
                if (currentInput != null) {
                    val projectedTotalToday = currentInput + state.todayAmount
                    val diff = projectedTotalToday - state.yesterdayAmount
                    val (diffText, diffColor) = when {
                        diff > 0 -> "+${diff.toCleanString()} ${state.product.unit} more than yesterday" to MaterialTheme.colorScheme.error
                        diff < 0 -> "${diff.toCleanString()} ${state.product.unit} less than yesterday" to MaterialTheme.colorScheme.primary
                        else -> "Same as yesterday" to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Column(modifier = Modifier.padding(top = 4.dp, start = 16.dp)) {
                        if (state.todayAmount > 0f) {
                            Text(
                                text = "Already consumed today: ${state.todayAmount.toCleanString()} ${state.product.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = diffText,
                            style = MaterialTheme.typography.bodySmall,
                            color = diffColor
                        )
                    }
                }
            }
        }
    }
}