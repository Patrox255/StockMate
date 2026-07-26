package com.example.stockmate.data.dtos

import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.prediction.ConsumptionPredictionEngine
import com.example.stockmate.data.prediction.ConsumptionPredictionResult
import com.example.stockmate.ui.state.prediction.PredictionUiState

data class ProductUiModel(
    val productWithMultipliers: ProductWithMultipliers,
    val predictionState: PredictionUiState
)

suspend fun ProductWithMultipliers.toUiModel(predictionEngine: ConsumptionPredictionEngine): ProductUiModel {
    val predictionRes = predictionEngine.predict(this.product.id)
    return ProductUiModel(
        productWithMultipliers = this,
        predictionState = if (predictionRes != null) {
            PredictionUiState.Success(predictionRes)
        } else {
            PredictionUiState.NotEnoughData
        }
    )
}