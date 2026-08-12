package com.example.stockmate.ui.screens.chart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.data.chart.StockChartProductsFilterMode
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.ui.components.chart.StockLogHistoryChart
import com.example.stockmate.ui.viewmodels.chart.StockLogChartTimeRange
import com.example.stockmate.ui.viewmodels.chart.StockLogChartViewModel

@Composable
fun StockLogHistoryScreen(
    viewModel: StockLogChartViewModel = hiltViewModel()
) {
    val legendItems by viewModel.legendItems.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val chartSettings by viewModel.chartSettings.collectAsState()
    val chartSeriesAdditionalRenderInfo by viewModel.chartSeriesAdditionalRenderInfo.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Product Stock Log History", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        TimeRangeSelector(
            selectedTimeRange = selectedTimeRange,
            onTimeRangeSelected = { viewModel.setTimeRange(it) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        StockLogHistoryChart(
            modelProducer = viewModel.modelProducer,
            chartSeriesAdditionalRenderInfo = chartSeriesAdditionalRenderInfo
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Products Legend",
                style = MaterialTheme.typography.titleMedium
            )
            FilterModeToggle(
                currentMode = chartSettings.filterMode,
                onModeChanged = { newMode -> viewModel.setFilterMode(newMode) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (legendItems.isEmpty()) {
            Text(
                text = "No stock activity in selected time range.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                legendItems.forEach { item ->
                    LegendItem(
                        name = item.name,
                        color = item.color,
                        isVisible = item.isVisible,
                        onClick = { viewModel.toggleProductVisibility(item.productId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ChangeReasonFilterSection(
            allowedChangedReasons = chartSettings.allowedChangedReasons,
            onReasonToggled = { reason -> viewModel.toggleChangeReasonVisibility(reason) }
        )
    }
}

@Composable
fun ChangeReasonFilterSection(
    allowedChangedReasons: List<ChangeReason>,
    onReasonToggled: (ChangeReason) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Included Operations",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChangeReason.entries.forEach { reason ->
                val isSelected = allowedChangedReasons.contains(reason)

                FilterChip(
                    selected = isSelected,
                    onClick = { onReasonToggled(reason) },
                    label = {
                        Text(
                            text = reason.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selectedTimeRange: StockLogChartTimeRange,
    onTimeRangeSelected: (StockLogChartTimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            StockLogChartTimeRange.entries.forEachIndexed { index, range ->
                SegmentedButton(
                    selected = range == selectedTimeRange,
                    onClick = { onTimeRangeSelected(range) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = StockLogChartTimeRange.entries.size
                    ),
                    icon = {}
                ) {
                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
@Composable
private fun FilterModeToggle(
    currentMode: StockChartProductsFilterMode,
    onModeChanged: (StockChartProductsFilterMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            selected = currentMode == StockChartProductsFilterMode.EXCLUDE,
            onClick = { onModeChanged(StockChartProductsFilterMode.EXCLUDE) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {}
        ) {
            Text("Hide clicked", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
        SegmentedButton(
            selected = currentMode == StockChartProductsFilterMode.INCLUDE,
            onClick = { onModeChanged(StockChartProductsFilterMode.INCLUDE) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {}
        ) {
            Text("Show clicked", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun LegendItem(
    name: String,
    color: Color,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    val surfaceColor = if (isVisible) color.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.05f)
    val borderColor = if (isVisible) color.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)
    val textColor = if (isVisible) MaterialTheme.colorScheme.onSurface else Color.Gray

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(if (isVisible) color else Color.Gray, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = name, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}