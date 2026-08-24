package com.example.stockmate.ui.components.chart

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.util.chart.StepLineInterpolator
import com.example.stockmate.ui.components.shapes.DiamondShape
import com.example.stockmate.ui.viewmodels.chart.ChartSeriesAdditionalRenderInfo
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StockLogHistoryChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    chartSeriesAdditionalRenderInfo: List<ChartSeriesAdditionalRenderInfo>,
    startTime: Long
) {
    val dateTimeAxisLabelFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
    val dateTimeMarkerFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy\nHH:mm") }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val defaultMarkerFormatter = remember { DefaultCartesianMarker.ValueFormatter.default() }
    val chartColors = chartSeriesAdditionalRenderInfo.map { it.color }
    val chartSeriesReasons = chartSeriesAdditionalRenderInfo.map { it.reasonsByX }

    val customMarker = rememberDefaultCartesianMarker(
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        label = rememberTextComponent(
            lineCount = 30,
            background = rememberShapeComponent(
                fill = Fill(MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(4.dp)
            ),
            padding = Insets(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
        ),
        valueFormatter = remember(chartSeriesAdditionalRenderInfo) {
            DefaultCartesianMarker.ValueFormatter {context, targets ->
                // In our case the default text is the stock level after the change registered in the log
                val defaultText = defaultMarkerFormatter.format(context, targets)
                val lineTarget = targets.firstOrNull() as? LineCartesianLayerMarkerTarget ?: return@ValueFormatter "$defaultText"

                val xValue = lineTarget.x
                val dateString = Instant.ofEpochMilli(xValue.toLong())
                    .atZone(ZoneId.systemDefault())
                    .format(dateTimeMarkerFormatter)

                val productLines = lineTarget.points.mapNotNull { point ->
                    val seriesIndex = point.entry.seriesIndex
                    val infoSize = chartSeriesAdditionalRenderInfo.size

                    if (infoSize == 0) return@mapNotNull null

                    // This is a workaround to handle the prediction series which correspond to each of the products
                    // and are added after the history series, so we need to map them back to the original product series index.
                    // This is safe because we know that the whole list size would be equal to the number of products
                    // times 2 (history + prediction), so the modulo operation will always give us a valid index in the original list.
                    val baseSeriesIndex = seriesIndex % infoSize
                    val isPrediction = seriesIndex >= infoSize
                    val relatedAdditionalInfo =
                        chartSeriesAdditionalRenderInfo.getOrNull(baseSeriesIndex)
                    val stockValue = point.entry.y
                    if (relatedAdditionalInfo == null) {
                        return@ValueFormatter "Unknown product: ${stockValue} (Unknown operation)"
                    }
                    val reason: ChangeReason? = if (relatedAdditionalInfo.reasonsByX.containsKey(point.entry.x))
                        relatedAdditionalInfo.reasonsByX.get(point.entry.x) else null
                    val productName = relatedAdditionalInfo.productName
                    Log.d("StockLogHistoryChart", "Marker for product: $productName, stockValue: $stockValue, time: ${point.entry.x}, curStockPointTime: ${relatedAdditionalInfo.curStockPointTime}, diff: ${relatedAdditionalInfo.curStockPointTime?.let { point.entry.x - it }}")

                    if (isPrediction) {
                        "${productName}: ${stockValue} (Predicted Stock)"
                    } else {
                        "${productName}: ${stockValue} (${
                            if (reason?.displayName != null) {
                                reason.displayName
                            } else if (relatedAdditionalInfo.curStockPointTime != null && xValue == relatedAdditionalInfo.curStockPointTime.toDouble()) {
                                "Current Stock"
                            } else if (xValue == startTime.toDouble()) {
                                "Start Stock"
                            } else {
                                "?"
                            }
                        })"
                    }
                }

                "${dateString}\n" + productLines.joinToString(separator = "\n")
            }
        }
    )

    val historyLines = chartColors.mapIndexed { index, color ->
        val seriesReasons = chartSeriesReasons.getOrNull(index) ?: emptyMap()
        val circlePoint = LineCartesianLayer.Point(
            component = rememberShapeComponent(
                shape = CircleShape,
                fill = Fill(color),
                strokeFill = Fill(surfaceColor),
                strokeThickness = 2.dp
            ),
            size = 8.dp
        )

        val squarePoint = LineCartesianLayer.Point(
            component = rememberShapeComponent(
                shape = RectangleShape,
                fill = Fill(color),
                strokeFill = Fill(surfaceColor),
                strokeThickness = 2.dp
            ),
            size = 8.dp
        )

        val diamondPoint = LineCartesianLayer.Point(
            component = rememberShapeComponent(
                shape = DiamondShape,
                fill = Fill(color),
                strokeFill = Fill(surfaceColor),
                strokeThickness = 2.dp
            ),
            size = 8.dp
        )

        val roundedSquarePoint = LineCartesianLayer.Point(
            component = rememberShapeComponent(
                shape = RoundedCornerShape(3.dp),
                fill = Fill(color),
                strokeFill = Fill(surfaceColor),
                strokeThickness = 2.dp
            ),
            size = 8.dp
        )

        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(color)),
            interpolator = StepLineInterpolator,
            pointProvider = remember(seriesReasons) {
                object : LineCartesianLayer.PointProvider {
                    override fun getPoint(
                        entry: LineCartesianLayerModel.Entry,
                        extraStore: ExtraStore
                    ): LineCartesianLayer.Point? {
                        val reason = chartSeriesReasons.getOrNull(index)?.get(entry.x)
                        return when (reason) {
                            ChangeReason.RESTOCKED -> squarePoint
                            ChangeReason.CONSUMED -> circlePoint
                            ChangeReason.WASTED -> diamondPoint
                            ChangeReason.ADJUSTED -> roundedSquarePoint
                            null -> null
                        }
                    }

                    override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point? {
                        // All of them are of the same size therefore we can just return one of them.
                        return circlePoint
                    }
                }
            }
        )
    }

    val predictionLines = chartColors.map {color ->
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(color)),
            stroke = LineCartesianLayer.LineStroke.Dashed(
                thickness = 2.dp,
            ),
            pointProvider = null
        )
    }

    val lineProvider = if (historyLines.isNotEmpty()) {
        LineCartesianLayer.LineProvider.series(historyLines + predictionLines)
    } else {
        LineCartesianLayer.LineProvider.series(LineCartesianLayer.rememberLine())
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = lineProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                ),
                title = { "Stock Level" }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                    spacing = {_ -> 4},
                    shiftExtremeLines = true,
                    addExtremeLabelPadding = true
                ),
                valueFormatter = { _, value, _ ->
                    Instant.ofEpochMilli(value.toLong())
                        .atZone(ZoneId.systemDefault())
                        .format(dateTimeAxisLabelFormatter)
                },
                label = rememberTextComponent(
                    style = TextStyle(
                        color  = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    ),
                    lineCount = 2
                ),
                guideline = null
            ),
            marker = customMarker,
            getXStep = { 24.0*60.0*60.0*1000.0 }
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        zoomState = rememberVicoZoomState(zoomEnabled = true, initialZoom = Zoom.Content),
    )
}