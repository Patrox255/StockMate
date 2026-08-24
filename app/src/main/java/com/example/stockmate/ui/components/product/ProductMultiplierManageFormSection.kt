package com.example.stockmate.ui.components.product

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.data.validationUtil.FieldErrorKey
import com.example.stockmate.data.validationUtil.formErrors
import com.example.stockmate.ui.components.form.FormTextField
import com.example.stockmate.ui.viewmodels.ProductFormViewModel

@Composable
fun ProductMultiplierManageFormSection(
    multiplier: MultiplierFormState,
    onUpdateMultiplier: (localId: String, name: String, value: String) -> Unit,
    onRemoveMultiplier: (localId: String) -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean,
    dragModifier: Modifier = Modifier,
    multipliersErrors: formErrors<ProductFormViewModel.MultiplierFormField> = emptyMap()
) {
    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "Elevation animation")
    val bgColor by animateColorAsState(
        if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        label = "Background color animation"
    )
    val getMultiplierFieldError: (field: ProductFormViewModel.MultiplierFormField) -> List<String>? = { field ->
        multipliersErrors[FieldErrorKey(
            itemId = multiplier.localId,
            field = field
        )]
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shadowElevation = elevation,
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Drag to reorder",
                modifier = dragModifier
                    .padding(8.dp)
            )

            FormTextField(
                value = multiplier.name,
                onValueChange = { onUpdateMultiplier(multiplier.localId, it, multiplier.value) },
                label = "Name (e.g. Pallet)",
                modifier = Modifier.weight(1f),
                errorMessages = getMultiplierFieldError(ProductFormViewModel.MultiplierFormField.NAME) ?: emptyList()
            )

            FormTextField(
                value = multiplier.value,
                onValueChange = { onUpdateMultiplier(multiplier.localId, multiplier.name, it) },
                label = "Value",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorMessages = getMultiplierFieldError(ProductFormViewModel.MultiplierFormField.VALUE) ?: emptyList(),
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
}