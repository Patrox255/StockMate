package com.example.stockmate.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.ui.components.FormTextField

@Composable
fun ProductMultipliersManageFormSection(
    multipliers: List<MultiplierFormState>,
    onAddMultiplier: () -> Unit,
    onUpdateMultiplier: (id: String, name: String, value: String) -> Unit,
    onRemoveMultiplier: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Multipliers (e.g. Box = 10 pcs)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        multipliers.forEach { multiplier ->
            ProductMultiplierManageFormSection(
                multiplier = multiplier,
                onUpdateMultiplier = onUpdateMultiplier,
                onRemoveMultiplier = onRemoveMultiplier
            )
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