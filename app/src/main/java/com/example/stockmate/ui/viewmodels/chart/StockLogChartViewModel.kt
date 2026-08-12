package com.example.stockmate.ui.viewmodels.chart

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.chart.StockChartManagerSettings
import com.example.stockmate.data.chart.StockChartProductsFilterMode
import com.example.stockmate.data.chart.StockChartLegendItemUiModel
import com.example.stockmate.data.chart.StockChartManager
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.repository.ProductRepository
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

enum class StockLogChartTimeRange(val rangeInMillis: Long, val displayName: String) {
    ONE_DAY(24 * 60 * 60 * 1000L, "Last 24 Hours"),
    SEVEN_DAYS(7 * 24 * 60 * 60 * 1000L, "Last 7 Days"),
    THIRTY_DAYS(30 * 24 * 60 * 60 * 1000L, "Last 30 Days"),
    NINETY_DAYS(90 * 24 * 60 * 60 * 1000L, "Last 90 Days"),
    ONE_HUNDRED_EIGHTY_DAYS(180 * 24 * 60 * 60 * 1000L, "Last 180 Days"),
    THREE_HUNDRED_SIXTY_FIVE_DAYS(365 * 24 * 60 * 60 * 1000L, "Last 365 Days")
}

data class ChartSeriesAdditionalRenderInfo(
    val color: Color,
    val reasonsByX: Map<Double, ChangeReason>,
    val productName: String
)

@HiltViewModel
class StockLogChartViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockLogDao: StockLogDao,
    private val stockChartManager: StockChartManager
) : ViewModel() {
    private val _selectedTimeRange = MutableStateFlow(StockLogChartTimeRange.THIRTY_DAYS)
    private val _chartSettings = MutableStateFlow(StockChartManagerSettings())
    private val _rawLogsFlow = _selectedTimeRange.flatMapLatest { range ->
        val startTime = System.currentTimeMillis() - range.rangeInMillis
        stockLogDao.getLogsSinceFlow(startTime)
    }
    private val _chartLines =  combine(
        _rawLogsFlow,
        _chartSettings
    ) { logs, settings ->
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - _selectedTimeRange.value.rangeInMillis
        stockChartManager.buildChartData(logs, settings, startTime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    // This flow is used to store the reasons, names and colors for product lines and their respective timestamps,
    // which will be used to display the reason for the change in stock when the user interacts with the
    // specific point within the chart. This is necessary because the chart model itself does not store this information
    private val _chartSeriesAdditionalRenderInfo = MutableStateFlow<List<ChartSeriesAdditionalRenderInfo>>(emptyList())

    val chartSettings = _chartSettings.asStateFlow()
    val selectedTimeRange = _selectedTimeRange.asStateFlow()
    val chartSeriesAdditionalRenderInfo = _chartLines.map { lines ->
        lines.map { line ->
            ChartSeriesAdditionalRenderInfo(
                color = line.color,
                reasonsByX = line.points.mapNotNull { pt ->
                    pt.reason?.let { reason ->
                        (pt.timestamp.toDouble()) to reason
                    }
                }.toMap(),
                productName = line.product.name
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val legendItems: StateFlow<List<StockChartLegendItemUiModel>> = combine(
        _rawLogsFlow,
        _chartSettings
    ) {logs, settings ->
        val uniqueProductIds = logs.map {it.productId}.distinct()
        val products = productRepository.getProductsByIds(uniqueProductIds)

        products.map { product ->
            val isVisible = if (settings.filterMode == StockChartProductsFilterMode.INCLUDE) {
                settings.includedOrExcludedProductsIds.contains(product.id)
            } else {
                !settings.includedOrExcludedProductsIds.contains(product.id)
            }
            StockChartLegendItemUiModel(
                productId = product.id,
                name = product.name,
                color = stockChartManager.generateStableColor(product.id),
                isVisible = isVisible
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val modelProducer = CartesianChartModelProducer()
    init {
        viewModelScope.launch {
            _chartLines.collect { chartLines ->
                // These series additional render info entries of course correspond to the series
                // in the chart model visible below, and will be used to deliver additional info
                // to the chart rendering component
                _chartSeriesAdditionalRenderInfo.value = chartLines.map { line ->
                    ChartSeriesAdditionalRenderInfo(
                        color = line.color,
                        reasonsByX = line.points.mapNotNull { pt ->
                            pt.reason?.let { reason ->
                                (pt.timestamp.toDouble()) to reason
                            }
                        }.toMap(),
                        productName = line.product.name
                    )
                }
                modelProducer.runTransaction {
                    lineModel {
                        if (chartLines.isNotEmpty()) {
                            chartLines.forEach { lineData ->
                                series(
                                    x = lineData.points.map { it.timestamp.toDouble() },
                                    y = lineData.points.map { it.value.toDouble() },
                                )
                            }
                        } else {
                            // Add a default series with only a single point in order to avoid displaying
                            // the chart when there is no data.
                            series(
                                x = listOf(System.currentTimeMillis()),
                                y = listOf(0f)
                            )
                        }
                    }
                }
            }
        }
    }

    fun setTimeRange(stockLogChartTimeRange: StockLogChartTimeRange) {
        _selectedTimeRange.value = stockLogChartTimeRange
    }
    fun setChartSettings(settings: StockChartManagerSettings) {
        _chartSettings.value = settings
    }
    fun setFilterMode(filterMode: StockChartProductsFilterMode) {
        val currentSettings = _chartSettings.value
        _chartSettings.value = currentSettings.copy(filterMode = filterMode)
    }
    fun toggleProductVisibility(productId: Long) {
        val currentSettings = _chartSettings.value
        val currentList = currentSettings.includedOrExcludedProductsIds.toMutableList()

        if (currentList.contains(productId)) {
            currentList.remove(productId)
        } else {
            currentList.add(productId)
        }
        _chartSettings.value = currentSettings.copy(includedOrExcludedProductsIds = currentList)
    }
    fun toggleChangeReasonVisibility(reason: ChangeReason) {
        val currentSettings = _chartSettings.value
        val currentList = currentSettings.allowedChangedReasons.toMutableList()

        if (currentList.contains(reason)) {
            currentList.remove(reason)
        } else {
            currentList.add(reason)
        }
        _chartSettings.value = currentSettings.copy(allowedChangedReasons = currentList)
    }
}