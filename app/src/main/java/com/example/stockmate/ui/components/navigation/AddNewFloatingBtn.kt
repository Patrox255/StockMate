package com.example.stockmate.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun AddNewFloatingBtn(
    onClick: () -> Unit,
    contentDescription: String
) {
    FloatingActionButton(
        onClick = { onClick() }
    ) {
        Icon(Icons.Default.Add, contentDescription = contentDescription)
    }
}