package com.example.stockmate.ui.screens.stock

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.stock.AutoConsumptionCard
import com.example.stockmate.ui.viewmodels.stock.AutoConsumptionUiEvent
import com.example.stockmate.ui.viewmodels.stock.AutoConsumptionViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AutoConsumptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AutoConsumptionViewModel = hiltViewModel()
) {
    val consumptionItems by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AutoConsumptionUiEvent.ShowToastAndNavigateBack -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (consumptionItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val hasAnySelected = consumptionItems.any {it.isSelected}
                    Button(
                        onClick = { viewModel.applyAutoConsumption() },
                        enabled = hasAnySelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                    ) {
                        Text("Apply deductions")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else if (consumptionItems.isEmpty()) {
                Text(
                    text = "No predictions or consumption history available.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(consumptionItems, key = {it.product.id}) { item ->
                        AutoConsumptionCard(
                            state = item,
                            onModeChanged = { newMode -> viewModel.onModeChanged(item.product.id, newMode) },
                            onAmountChanged = { newAmount -> viewModel.onAmountInputChanged(item.product.id, newAmount) },
                            onToggleSelection = { viewModel.onToggleSelection(item.product.id) },
                            onDishToggled = viewModel::onDishToggled,
                            onApplyDishToAll = viewModel::applyDishContextToAll
                        )
                    }
                }
            }
        }
    }
}