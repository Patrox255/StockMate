package com.example.stockmate.ui.screens.inventory

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.ui.components.product.ProductNoMultipliersConfiguredMessage
import com.example.stockmate.ui.components.product.SelectStockAdjustmentReasonDialog
import com.example.stockmate.ui.components.product.StockAdjustmentControls
import com.example.stockmate.ui.viewmodels.InventoryViewModel

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateToDetails: (productId: Long) -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddProduct() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add a new product")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(products) {
                    product ->
                ProductItem(productWithMultipliers = product, viewModel = viewModel, onNavigateToDetails = onNavigateToDetails)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProductItem(productWithMultipliers: ProductWithMultipliers, viewModel: InventoryViewModel, onNavigateToDetails: (productId: Long) -> Unit) {
    var (product, multipliers) = productWithMultipliers
    var showDialog by remember { mutableStateOf(false) }
    var pendingDifference by remember {mutableStateOf(0f)}
    var inputText by remember {mutableStateOf(product.currentStock.toString())}
    var selectedMultiplier by remember {mutableStateOf(multipliers.firstOrNull())}

    LaunchedEffect(product.currentStock) {
        inputText = product.currentStock.toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {onNavigateToDetails(product.id)},
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = product.name, style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(4.dp))

                    val isLowStock = product.currentStock < product.targetStock
                    Row {
                        Text(
                            text = "Stock: ${product.currentStock}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " / ${product.targetStock} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(text = "Goal: ${product.targetStock} ${product.unit}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            selectedMultiplier?.let { safeMultiplier ->
                StockAdjustmentControls(
                    multipliers = multipliers,
                    currentMultiplier = safeMultiplier,
                    onMultiplierSelected = { multiplier ->
                        selectedMultiplier = multiplier
                    },
                    onMinusClick = {
                        pendingDifference = -safeMultiplier.value
                        showDialog = true
                    },
                    onPlusClick = {
                        pendingDifference = safeMultiplier.value
                        showDialog = true
                    },
                    productUnit = product.unit
                )
            } ?: run {
                ProductNoMultipliersConfiguredMessage()
            }
        }
    }

    if (showDialog) {
        SelectStockAdjustmentReasonDialog(
            onDismiss = {
                showDialog = false
                inputText = product.currentStock.toString()
            },
            onReasonSelected = { reason ->
                viewModel.changeStock(product, pendingDifference, reason)
                showDialog = false
            }
        )
    }
}