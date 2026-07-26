package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dtos.ProductUiModel
import com.example.stockmate.data.dtos.toUiModel
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.prediction.ConsumptionPredictionEngine
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.product.ProductStockManager
import com.example.stockmate.data.util.search.FilterGroup
import com.example.stockmate.data.util.search.FilterOption
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import com.example.stockmate.data.util.search.SingleSelectFilterGroup
import com.example.stockmate.data.util.search.SortOption
import com.example.stockmate.ui.state.prediction.PredictionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Comparator.nullsLast
import javax.inject.Inject
import kotlin.comparisons.compareBy
import kotlin.comparisons.nullsLast
import kotlin.comparisons.reverseOrder

@HiltViewModel
class InventoryViewModel @Inject constructor (
    private val productRepository: ProductRepository,
    private val predictionEngine: ConsumptionPredictionEngine,
    val productStockManager: ProductStockManager
): ViewModel() {
    companion object {
        const val DEBOUNCE_DELAY_SECONDS = 0.3
        const val SEARCH_STOP_TIMEOUT_SECONDS = 5
    }

    val availableSorts = listOf(
        SortOption<ProductUiModel>(
            id = "name",
            displayName = "Name",
            ascendingComparator = compareBy { it.productWithMultipliers.product.name.lowercase() }
        ),
        SortOption<ProductUiModel>(
            id = "stock_pct",
            displayName = "Stock %",
            ascendingComparator = compareBy { it.productWithMultipliers.product.stockPercentage }
        ),
        SortOption<ProductUiModel>(
            id = "days_until_empty",
            displayName = "Days until empty",
            ascendingComparator = compareBy(nullsLast<Double>()) { getDaysUntilEmpty(it) },
            descendingComparator = compareBy(kotlin.comparisons.nullsLast(reverseOrder<Double>())) { getDaysUntilEmpty(it) }
        ),
        SortOption<ProductUiModel>(
            id = "daily_consumption",
            displayName = "Daily consumption",
            ascendingComparator = compareBy(nullsLast<Double>()) { getDailyConsumption(it) },
            descendingComparator = compareBy(kotlin.comparisons.nullsLast(reverseOrder<Double>())) { getDailyConsumption(it) }
        )
    )

    private fun getDaysUntilEmpty(model: ProductUiModel): Double? {
        val days = (model.predictionState as? PredictionUiState.Success)?.result?.predictedDaysUntilEmpty
        // Infinity indicates that the predicted daily consumption is zero.
        return if (days == Double.POSITIVE_INFINITY) null else days
    }

    private fun getDailyConsumption(model: ProductUiModel): Double? {
        return (model.predictionState as? PredictionUiState.Success)?.result?.predictedDailyConsumption
    }

    private val initialFilters = listOf<FilterGroup<ProductUiModel>>(
        SingleSelectFilterGroup(
            id = "stock_status",
            name = "Stock Status",
            options = listOf(
                FilterOption(
                    "to_restock",
                    "To restock"
                ) { it.productWithMultipliers.product.currentStock < it.productWithMultipliers.product.targetStock },
                FilterOption(
                    "in_stock",
                    "In stock"
                ) { it.productWithMultipliers.product.currentStock >= it.productWithMultipliers.product.targetStock },
                FilterOption("out_of_stock", "Out of stock")
                { it.productWithMultipliers.product.currentStock <= 0f }
            )
        )
    )

    val listEngine = SearchSortFilterEngine<ProductUiModel>(
        initialFilterGroups = initialFilters,
        searchMatcher = { item, query ->
            item.productWithMultipliers.product.name.contains(query, ignoreCase = true)
        },
        stopTimeoutMillis = SEARCH_STOP_TIMEOUT_SECONDS * 1000L,
        debounceDelaySeconds = DEBOUNCE_DELAY_SECONDS
    )

    val productsSourceFlow = productRepository.getAllProductsWithMultipliersFlow()
        .map {products ->
            products.map { productWithMultipliers ->
                productWithMultipliers.toUiModel(predictionEngine)
            }
        }

    val displayedProducts = listEngine.process(
        sourceFlow = productsSourceFlow,
        scope = viewModelScope
    )

    fun onReasonSelected(product: Product, reason: ChangeReason) {
        viewModelScope.launch {
            productStockManager.StockAdjustmentDialogOnReasonSelected(product, reason)
        }
    }
}