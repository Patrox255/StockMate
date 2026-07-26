package com.example.stockmate.ui.screens.inventory

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.util.img.ImgDisplayGuidelines
import com.example.stockmate.data.util.img.ProductImgDisplayGuidelines
import com.example.stockmate.ui.components.img.ImgDisplay
import com.example.stockmate.ui.components.product.InventoryScreenProductItem
import com.example.stockmate.ui.components.product.ProductNoMultipliersConfiguredMessage
import com.example.stockmate.ui.components.product.SelectStockAdjustmentReasonDialog
import com.example.stockmate.ui.components.product.StockAdjustmentControls
import com.example.stockmate.ui.components.search.FilterSortBar
import com.example.stockmate.ui.components.search.SearchBar
import com.example.stockmate.ui.viewmodels.InventoryViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateToDetails: (productId: Long) -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    val products = viewModel.displayedProducts.collectAsState().value
    val activeSorts by viewModel.listEngine.activeSorts.collectAsState()
    val filterGroups by viewModel.listEngine.filterGroups.collectAsState()
    val searchQuery by viewModel.listEngine.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddProduct() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add a new product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    viewModel.listEngine.updateSearchQuery(query)
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

            if (products.isEmpty()) {
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
                    items(products) { product ->
                        ProductItem(
                            productWithMultipliers = product,
                            viewModel = viewModel,
                            onNavigateToDetails = onNavigateToDetails
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    productWithMultipliers: ProductWithMultipliers,
    viewModel: InventoryViewModel,
    onNavigateToDetails: (productId: Long) -> Unit
) {
    val (product, multipliers) = productWithMultipliers

    var pendingDifference by remember { mutableStateOf(0f) }
    var selectedMultiplier by remember { mutableStateOf(multipliers.firstOrNull()) }
    var showDialog by remember {mutableStateOf(false)}

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

    if (showDialog) {
        SelectStockAdjustmentReasonDialog(
            onDismiss = {
                showDialog = false
            },
            onReasonSelected = { reason ->
                viewModel.changeStock(product, pendingDifference, reason)
                showDialog = false
            }
        )
    }
}