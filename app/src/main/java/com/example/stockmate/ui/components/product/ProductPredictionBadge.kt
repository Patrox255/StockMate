package com.example.stockmate.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.state.prediction.PredictionUiState

@Composable
fun ProductPredictionBadge(
    predictionState: PredictionUiState,
    modifier: Modifier = Modifier
) {
    when (predictionState) {
        is PredictionUiState.Loading -> {
            LoadingIndicator()
        }

        is PredictionUiState.NotEnoughData -> {}

        is PredictionUiState.Success -> {
            val prediction = predictionState.result
            val daysText = if (prediction.predictedDaysUntilEmpty.isInfinite()) {
                "No usage registered"
            } else {
                "~${prediction.predictedDaysUntilEmpty.toInt()}d left"
            }
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Forecast",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}