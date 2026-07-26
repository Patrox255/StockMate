package com.example.stockmate.ui.state.prediction

import com.example.stockmate.data.prediction.ConsumptionPredictionResult

sealed interface PredictionUiState {
    object Loading : PredictionUiState
    object NotEnoughData : PredictionUiState
    data class Success(val result: ConsumptionPredictionResult) : PredictionUiState
}