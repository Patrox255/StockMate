package com.example.stockmate.ui.components.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.util.search.FilterGroup
import com.example.stockmate.data.util.search.MultiSelectFilterGroup
import com.example.stockmate.data.util.search.SingleSelectFilterGroup

@Composable
fun <T> FilterGroupChip(
    group: FilterGroup<T>,
    onOptionToggled: (optionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val labelText = remember(group) {
        when (group) {
            is SingleSelectFilterGroup -> {
                val selected = group.options.find { it.id == group.selectedOptionId }
                if (selected != null) "${group.name}: ${selected.label}" else group.name
            }
            is MultiSelectFilterGroup -> {
                val count = group.selectedOptionIds.size
                if (count > 0) "${group.name} ($count)" else group.name
            }
        }
    }

    val isSelected = remember(group) {
        when (group) {
            is SingleSelectFilterGroup -> group.selectedOptionId != null
            is MultiSelectFilterGroup -> group.selectedOptionIds.isNotEmpty()
        }
    }

    Box(modifier = modifier) {
        FilterChip(
            selected = isSelected,
            onClick = { expanded = true },
            label = { Text(labelText) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            group.options.forEach { option ->
                val isOptionSelected = when (group) {
                    is SingleSelectFilterGroup -> group.selectedOptionId == option.id
                    is MultiSelectFilterGroup -> group.selectedOptionIds.contains(option.id)
                }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (group is MultiSelectFilterGroup) {
                                Checkbox(
                                    checked = isOptionSelected,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = option.label,
                                fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onOptionToggled(option.id)
                        if (group is SingleSelectFilterGroup) expanded = false
                    },
                    trailingIcon = {
                        if (group is SingleSelectFilterGroup && isOptionSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }
        }
    }
}