package com.example.stockmate.data.chart

import androidx.compose.ui.graphics.Color
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel

data class StockChartLineData(
    val product: Product,
    val points: List<StockChartPoint>,
    val color: Color
)

data class StockChartPoint(
    val timestamp: Long,
    val value: Float,
    val reason: ChangeReason? = null
)

data class StockChartLegendItemUiModel(
    val productId: Long,
    val name: String,
    val color: Color,
    val isVisible: Boolean
)

// This is the data class that will be used to represent each entry in the chart as it supports also
// the reason for the change in stock, which is important for the chart to display that additional information
// on interaction.
data class StockChartEntry(
    override val x: Double,
    val y: Double,
    val reason: ChangeReason? = null
) : CartesianLayerModel.Entry