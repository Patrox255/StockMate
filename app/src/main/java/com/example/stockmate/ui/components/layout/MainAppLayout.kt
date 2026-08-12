package com.example.stockmate.ui.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockmate.Destinations
import com.example.stockmate.destinationToHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    content: @Composable (PaddingValues) -> Unit,
    onNavigateBack: () -> Unit,
    currentRoute: String?,
    dynamicTitle: String?,
    dynamicActions: @Composable (RowScope.() -> Unit)? = null,
    dynamicNavigationIcon: @Composable (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = dynamicTitle
                            ?: destinationToHeader(currentRoute),
                    )
                },
                navigationIcon = {
                    if (dynamicNavigationIcon == null) {
                        IconButton(onClick = { onNavigateBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    else {
                        dynamicNavigationIcon()
                    }
                },
                actions = {
                    if (dynamicActions != null) {
                        dynamicActions()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (currentRoute == Destinations.SETTINGS)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}