package com.example.stockmate.ui.viewmodels.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dao.DishConsumptionBreakdown
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.prediction.ConsumptionPredictionEngine
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.formatting.toCleanString
import com.example.stockmate.data.util.formatting.toFloatOrZero
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.abs

enum class SuggestionMode {
    PREDICTION,
    YESTERDAY,
    EQUALIZE_TO_YESTERDAY
}

data class AutoConsumptionItemState(
    val product: Product,
    val predictedAmount: Float,
    val yesterdayAmount: Float,
    val selectedMode: SuggestionMode,
    val amountInput: String,
    val isSelected: Boolean = true,
    val yesterdayDishes: List<DishConsumptionBreakdown> = emptyList(),
    val selectedDishes: Set<String> = emptySet(),
    val recentToggledDish: String? = null,
    val todayAmount: Float = 0f
)

sealed interface AutoConsumptionUiEvent {
    data class ShowToastAndNavigateBack(val message: String): AutoConsumptionUiEvent
}

@HiltViewModel
class AutoConsumptionViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockLogDao: StockLogDao,
    private val predictionEngine: ConsumptionPredictionEngine
): ViewModel() {
    companion object {
        val DEFAULT_SUGGESTION_MODE = SuggestionMode.YESTERDAY
    }

    private val _items = MutableStateFlow<List<AutoConsumptionItemState>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _uiEvent = MutableSharedFlow<AutoConsumptionUiEvent>()

    val items = _items.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadSuggestions()
    }

    private fun getDefaultProductSuggestionMode(yesterdayConsumption: Float, predictedConsumption: Float): SuggestionMode {
        return if (DEFAULT_SUGGESTION_MODE == SuggestionMode.YESTERDAY && yesterdayConsumption > 0f) {
            SuggestionMode.YESTERDAY
        } else if (DEFAULT_SUGGESTION_MODE == SuggestionMode.PREDICTION && predictedConsumption > 0f) {
            SuggestionMode.PREDICTION
        } else if (yesterdayConsumption > 0f) {
            SuggestionMode.YESTERDAY
        } else {
            SuggestionMode.PREDICTION
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            val yesterday = LocalDate.now().minusDays(1)
            val zone = ZoneId.systemDefault()
            val startOfYesterday = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfYesterday = yesterday.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            val startOfToday = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
            val curTime = System.currentTimeMillis()

            val yesterdayConsumption = stockLogDao.getConsumptionBetween(startOfYesterday, endOfYesterday)
                .associateBy ({ it.productId }, {it.totalConsumed})
            val todayConsumption = stockLogDao.getConsumptionBetween(startOfToday, curTime)
                .associateBy ({ it.productId }, {it.totalConsumed})
            val allProducts = productRepository.getAllProductsFlow().first()
            val loadedItems = mutableListOf<AutoConsumptionItemState>()

            allProducts.forEach { product ->
                val predicted = predictionEngine.predict(product.id)?.predictedDailyConsumption?.toFloat()
                    ?: 0f
                val todayVal = todayConsumption[product.id] ?: 0f
                val yesterdayVal = yesterdayConsumption[product.id] ?: 0f
                if (predicted > 0f || yesterdayVal > 0f) {
                    val initialMode = getDefaultProductSuggestionMode(yesterdayVal, predicted)
                    val initialAmount = if (initialMode == SuggestionMode.YESTERDAY) yesterdayVal else predicted
                    val dishBreakdown = if (yesterdayVal > 0f) {
                        stockLogDao.getYesterdayDishBreakdown(product.id, startOfYesterday, endOfYesterday)
                    } else {
                        emptyList()
                    }
                    loadedItems.add(
                        AutoConsumptionItemState(
                            product = product,
                            predictedAmount = predicted,
                            yesterdayAmount = yesterdayVal,
                            selectedMode = initialMode,
                            yesterdayDishes = dishBreakdown,
                            amountInput = initialAmount.toString(),
                            todayAmount = todayVal
                        )
                    )
                }
            }

            _items.value = loadedItems
            _isLoading.value = false
        }
    }

    fun onModeChanged(productId: Long, newMode: SuggestionMode) {
        updateItemById(productId) { item ->
            val newAmount = when (newMode) {
                SuggestionMode.PREDICTION -> item.predictedAmount
                SuggestionMode.YESTERDAY -> item.yesterdayAmount
                SuggestionMode.EQUALIZE_TO_YESTERDAY -> item.yesterdayAmount - item.todayAmount
            }
            val allDishNames = item.yesterdayDishes.map { it.dishName }.toSet()
            val selectedDishes = if (newMode == SuggestionMode.YESTERDAY) {
                allDishNames
            } else {
                emptySet()
            }
            item.copy(selectedMode = newMode, amountInput = newAmount.toString(), selectedDishes = selectedDishes, recentToggledDish = null)
        }
    }

    fun onAmountInputChanged(productId: Long, newAmountInput: String) {
        val sanitizedInput = newAmountInput.replace("-", "")
        updateItemById(productId) { item ->
            item.copy(amountInput = sanitizedInput, recentToggledDish = null, selectedDishes = emptySet())
        }
    }

    fun onToggleSelection(productId: Long) {
        updateItemById(productId) { item ->
            item.copy(isSelected = !item.isSelected, recentToggledDish = null, selectedDishes = emptySet())
        }
    }

    fun onDishToggled(productId: Long, dishName: String) {
        _items.value = _items.value.map {item ->
            if (item.product.id == productId) {
                val newSelectedDishes = if (item.selectedDishes.contains(dishName)) {
                    item.selectedDishes - dishName
                } else {
                    item.selectedDishes + dishName
                }

                val newAmount = calcItemAmountBasedOnSelectedDishes(item, newSelectedDishes)

                item.copy(
                    selectedDishes = newSelectedDishes,
                    amountInput = newAmount.toCleanString(),
                    recentToggledDish = dishName,
                    selectedMode = SuggestionMode.YESTERDAY
                )
            } else {
                item
            }
        }
    }

    fun applyDishContextToAll(dishName: String) {
        val isDishEnabled = _items.value.find {it.recentToggledDish == dishName}?.selectedDishes?.contains(dishName) ?: false

        _items.value = _items.value.map { item ->
            val hasThisDish = item.yesterdayDishes.any { it.dishName == dishName }
            if (hasThisDish) {
                val newSelected = if (isDishEnabled) {
                    item.selectedDishes + dishName
                } else {
                    item.selectedDishes - dishName
                }

                val newAmount = calcItemAmountBasedOnSelectedDishes(item, newSelected)

                item.copy(
                    selectedDishes = newSelected,
                    amountInput = newAmount.toCleanString(),
                    selectedMode = SuggestionMode.YESTERDAY,
                    isSelected = true,
                    recentToggledDish = null
                )
            } else {
                item.copy(recentToggledDish = null)
            }
        }
    }

    private fun calcItemAmountBasedOnSelectedDishes(
        item: AutoConsumptionItemState,
        newSelectedDishes: Set<String>
    ): Float {
        val newAmount = item.yesterdayDishes
            .filter { it.dishName in newSelectedDishes }
            .sumOf { it.amountConsumed.toDouble() }
            .toFloat()
        return newAmount
    }

    private fun updateItemById(productId: Long, update: (AutoConsumptionItemState) -> AutoConsumptionItemState) {
        _items.value = _items.value.map { item ->
            if (item.product.id == productId) {
                update(item)
            } else {
                item
            }
        }
    }

    fun applyAutoConsumption() {
        viewModelScope.launch {
            _items.value
                .filter { it.isSelected }
                .forEach { item ->
                    val amountToDeduct = abs(item.amountInput.toFloatOrZero())

                    if (amountToDeduct > 0f) {
                        productRepository.changeStock(
                            product = item.product,
                            amount = -amountToDeduct,
                            reason = ChangeReason.CONSUMED,
                            relatedDishId = null,
                            relatedDishName = null
                        )
                    }
                }

            _uiEvent.emit(AutoConsumptionUiEvent.ShowToastAndNavigateBack("Auto consumption applied successfully."))        }

    }
}