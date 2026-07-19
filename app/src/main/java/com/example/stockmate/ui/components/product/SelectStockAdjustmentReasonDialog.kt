package com.example.stockmate.ui.components.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stockmate.data.entity.ChangeReason

@Composable
fun SelectStockAdjustmentReasonDialog(
    onDismiss: () -> Unit,
    onReasonSelected: (ChangeReason) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("What happened to the product?")
        },
        text = {
            Column {
                ChangeReason.entries.forEach { reason ->
                    TextButton(
                        onClick = {
                            onReasonSelected(reason)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = reason.displayName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}