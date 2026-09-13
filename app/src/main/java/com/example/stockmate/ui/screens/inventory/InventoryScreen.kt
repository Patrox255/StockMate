package com.example.stockmate.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.dtos.ProductUiModel
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.navigation.AddNewFloatingBtn
import com.example.stockmate.ui.components.product.InventoryScreenProductItem
import com.example.stockmate.ui.components.product.ProductNoMultipliersConfiguredMessage
import com.example.stockmate.ui.components.product.ProductPredictionBadge
import com.example.stockmate.ui.components.product.SelectStockAdjustmentReasonDialog
import com.example.stockmate.ui.components.product.StockAdjustmentControls
import com.example.stockmate.ui.components.search.FilterSortBar
import com.example.stockmate.ui.components.search.SearchBar
import com.example.stockmate.ui.components.selection.PaginationBar
import com.example.stockmate.ui.viewmodels.InventoryViewModel

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateToDetails: (productId: Long) -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    val paginationState by viewModel.paginationState.collectAsState()
    val activeSorts by viewModel.listEngine.activeSorts.collectAsState()
    val filterGroups by viewModel.listEngine.filterGroups.collectAsState()
    val searchQuery by viewModel.listEngine.searchQuery.collectAsState()
    val showDialog by viewModel.productStockManager.showDialog.collectAsState()
    val pendingDifference by viewModel.productStockManager.pendingDifference.collectAsState()

    Scaffold(
        floatingActionButton = {
            AddNewFloatingBtn(
                onClick = { onNavigateToAddProduct() },
                contentDescription = "Add a new product"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    viewModel.onSearchQueryChanged(query)
                },
                placeholder = "Search products by name..."
            )

            FilterSortBar(
                activeSorts = activeSorts,
                filterGroups = filterGroups,
                onFilterOptionToggled = viewModel.listEngine::onFilterOptionToggled,
                onToggleSort = viewModel.listEngine::toggleSort,
                availableSorts = viewModel.availableSorts
            )

            if (paginationState.isLoading) {
                LoadingIndicator()
            }
            else if (paginationState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "No products found",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No products found.",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try changing your filters or add a new product by tapping the '+' button.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(paginationState.items) { product ->
                        ProductItem(
                            productUiModel = product,
                            viewModel = viewModel,
                            onNavigateToDetails = onNavigateToDetails
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            PaginationBar(
                currentPage = paginationState.currentPage,
                visiblePages = paginationState.visiblePages,
                onPageSelected = { viewModel.paginationManager.goToPage(it) }
            )
        }
    }


    if (showDialog) {
        SelectStockAdjustmentReasonDialog(
            onDismiss = viewModel.productStockManager::StockAdjustmentDialogOnDismiss,
            onReasonSelected = {reason ->
                viewModel.onReasonSelected(reason)
            },
            pendingDifference = pendingDifference
        )
    }
}

@Composable
fun ProductItem(
    productUiModel: ProductUiModel,
    viewModel: InventoryViewModel,
    onNavigateToDetails: (productId: Long) -> Unit
) {
    val productWithMultipliers = productUiModel.productWithMultipliers
    val (product, multipliers) = productWithMultipliers

    var selectedMultiplier by remember { mutableStateOf(multipliers.firstOrNull()) }

    LaunchedEffect(multipliers) {
        selectedMultiplier =
            multipliers.find { it.id == selectedMultiplier?.id } ?: multipliers.firstOrNull()
    }

    InventoryScreenProductItem(
        productWithMultipliers,
        onNavigateToDetails
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        selectedMultiplier?.let { safeMultiplier ->
            StockAdjustmentControls(
                multipliers = multipliers,
                currentMultiplier = safeMultiplier,
                onMultiplierSelected = { multiplier ->
                    selectedMultiplier = multiplier
                },
                onMinusClick = {
                    viewModel.productStockManager.StockAdjustmentControlsOnMinusClick(product, selectedMultiplier)
                },
                onPlusClick = {
                    viewModel.productStockManager.StockAdjustmentControlsOnPlusClick(product, selectedMultiplier)
                },
                productUnit = product.unit
            )
        } ?: run {
            ProductNoMultipliersConfiguredMessage()
        }

        Spacer(modifier = Modifier.height(6.dp))
        ProductPredictionBadge(
            predictionState = productUiModel.predictionState
        )
    }
}