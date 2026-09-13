package com.example.stockmate.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.data.validationUtil.FieldErrorKey
import com.example.stockmate.data.validationUtil.formErrors
import com.example.stockmate.ui.viewmodels.ProductFormViewModel
import sh.calvin.reorderable.ReorderableColumn

@Composable
fun ProductMultipliersManageFormSection(
    multipliers: List<MultiplierFormState>,
    onAddMultiplier: () -> Unit,
    onUpdateMultiplier: (localId: String, name: String, value: String) -> Unit,
    onRemoveMultiplier: (localId: String) -> Unit,
    onMultiplierMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    multipliersErrors: formErrors<ProductFormViewModel.MultiplierFormField> = emptyMap()
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Multipliers (e.g. Box = 10 pcs)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ReorderableColumn(
            list = multipliers,
            onSettle = { fromIndex, toIndex ->
                onMultiplierMove(fromIndex, toIndex)
            }
        ) { _, multiplier, isDragging ->
            key(multiplier.localId) {
                ReorderableItem {
                    ProductMultiplierManageFormSection(
                        multiplier = multiplier,
                        isDragging = isDragging,
                        dragModifier = Modifier.draggableHandle(
                            onDragStarted = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ),
                        onUpdateMultiplier = onUpdateMultiplier,
                        onRemoveMultiplier = onRemoveMultiplier,
                        multipliersErrors = multipliersErrors
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onAddMultiplier,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add a multiplier entry")
        }
    }
}