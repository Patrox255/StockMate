package com.example.stockmate.ui.components.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.stockmate.data.entity.ProductMultiplier

@Composable
fun StockAdjustmentControls(
    multipliers: List<ProductMultiplier>,
    currentMultiplier: ProductMultiplier,
    onMultiplierSelected: (ProductMultiplier) -> Unit,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    productUnit: String
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = {
            onMinusClick()
        }) {
            Text("-${currentMultiplier.value} ${productUnit}")
        }

        Box {
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = {isDropdownExpanded = false}
            ) {
                multipliers.forEach { multiplier ->
                    DropdownMenuItem(
                        text = {Text("${multiplier.name} (${multiplier.value})")},
                        onClick = {
                            onMultiplierSelected(multiplier)
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Button(onClick = {
            onPlusClick()
        }) {
            Text("+${currentMultiplier.value} ${productUnit}")
        }
    }
}
