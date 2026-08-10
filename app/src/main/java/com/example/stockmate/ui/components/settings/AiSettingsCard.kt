package com.example.stockmate.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.dtos.AppSettings
import com.example.stockmate.data.prediction.PredictionModelType

@Composable
fun AiSettingsCard(
    settings: AppSettings,
    onUpdate: (AppSettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AiModelSelectionCard (
                currentModel = settings.predictionSelectedModel,
                onModelSelected = { newModel ->
                    onUpdate(settings.copy(predictionSelectedModel = newModel))
                }
            )

            AnimatedVisibility(visible = settings.predictionSelectedModel == PredictionModelType.CONSUMPTION_RANDOM_FOREST) {
                val rfSettings = settings.randomForestSettings
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Random Forest Hyperparameters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    VerticalDivider()
                    HyperparameterSlider(
                        title = "Number of trees",
                        description = "More trees increase accuracy but take longer to calculate.",
                        value = rfSettings.numTrees.toFloat(),
                        valueRange = 1f..50f,
                        steps = 49,
                        onValueChange = { newValue ->
                            onUpdate(
                                settings.copy(
                                    randomForestSettings = rfSettings.copy(
                                        numTrees = newValue.toInt()
                                    )
                                )
                            )
                        }
                    )
                    HyperparameterSlider(
                        title = "Max tree depth",
                        description = "How deep each decision tree can go. Prevents too shallow decision.",
                        value = rfSettings.maxDepth.toFloat(),
                        valueRange = 1f..10f,
                        steps = 9,
                        onValueChange = { newValue ->
                            onUpdate(
                                settings.copy(
                                    randomForestSettings = rfSettings.copy(
                                        maxDepth = newValue.toInt()
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AiModelSelectionCard(
    currentModel: PredictionModelType,
    onModelSelected: (PredictionModelType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = currentModel == PredictionModelType.CONSUMPTION_LINEAR_REGRESSION,
                    onClick = { onModelSelected(PredictionModelType.CONSUMPTION_LINEAR_REGRESSION) }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("Linear Regression", fontWeight = FontWeight.Bold)
                    Text("Fast, simple trend extrapolation.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = currentModel == PredictionModelType.CONSUMPTION_RANDOM_FOREST,
                    onClick = { onModelSelected(PredictionModelType.CONSUMPTION_RANDOM_FOREST) }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("Random Forest", fontWeight = FontWeight.Bold)
                    Text("Complex though more resource-heavy pattern recognition.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun HyperparameterSlider(
    title: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}