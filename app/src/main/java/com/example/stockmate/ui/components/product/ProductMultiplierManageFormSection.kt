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
import com.example.stockmate.data.dtos.MultiplierField
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.ui.components.FormTextField

@Composable
fun ProductMultiplierManageFormSection(
    multiplier: MultiplierFormState,
    onUpdateMultiplier: (id: String, name: String, value: String) -> Unit,
    onRemoveMultiplier: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormTextField(
            value = multiplier.name,
            onValueChange = { onUpdateMultiplier(multiplier.localId, it, multiplier.value) },
            label = "Name (e.g. Pallet)",
            modifier = Modifier.weight(1f),
            errorMessages = multiplier.errors[MultiplierField.NAME] ?: emptyList()
        )

        FormTextField(
            value = multiplier.value,
            onValueChange = { onUpdateMultiplier(multiplier.localId, multiplier.name, it) },
            label = "Value",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            errorMessages = multiplier.errors[MultiplierField.VALUE] ?: emptyList(),
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = { onRemoveMultiplier(multiplier.localId) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove multiplier",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}