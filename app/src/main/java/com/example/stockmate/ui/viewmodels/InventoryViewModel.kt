package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.img.ImageStorage
import com.example.stockmate.data.util.search.FilterGroup
import com.example.stockmate.data.util.search.FilterOption
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import com.example.stockmate.data.util.search.SingleSelectFilterGroup
import com.example.stockmate.data.util.search.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor (
    private val productRepository: ProductRepository,
    private val imageStorage: ImageStorage,
): ViewModel() {
    companion object {
        const val DEBOUNCE_DELAY_SECONDS = 0.3
        const val SEARCH_STOP_TIMEOUT_SECONDS = 5
    }

    val availableSorts = listOf(
        SortOption<ProductWithMultipliers>(
            id = "name",
            displayName = "Name",
            comparator = compareBy { it.product.name.lowercase() }
        ),
        SortOption<ProductWithMultipliers>(
            id = "stock_pct",
            displayName = "Stock %",
            comparator = compareBy { it.product.stockPercentage }
        )
    )

    private val initialFilters = listOf<FilterGroup<ProductWithMultipliers>>(
        SingleSelectFilterGroup(
            id = "stock_status",
            name = "Stock Status",
            options = listOf(
                FilterOption(
                    "to_restock",
                    "To restock"
                ) { it.product.currentStock < it.product.targetStock },
                FilterOption(
                    "in_stock",
                    "In stock"
                ) { it.product.currentStock >= it.product.targetStock },
                FilterOption("out_of_stock", "Out of stock")
                { it.product.currentStock <= 0f }
            )
        )
    )

    val listEngine = SearchSortFilterEngine<ProductWithMultipliers>(
        initialFilterGroups = initialFilters,
        searchMatcher = { item, query ->
            item.product.name.contains(query, ignoreCase = true)
        },
        stopTimeoutMillis = SEARCH_STOP_TIMEOUT_SECONDS * 1000L,
        debounceDelaySeconds = DEBOUNCE_DELAY_SECONDS
    )

    val displayedProducts = listEngine.process(
        sourceFlow = productRepository.getAllProductsWithMultipliersFlow(),
        scope = viewModelScope
    )

    fun changeStock(product: Product, amount: Float, reason: ChangeReason) {
        viewModelScope.launch {
            productRepository.changeStock(product, amount, reason)
        }
    }
}