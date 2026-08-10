package com.example.stockmate.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.prediction.ConsumptionPredictionEngine
import com.example.stockmate.data.prediction.ConsumptionPredictionResult
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.product.ProductStockManager
import com.example.stockmate.ui.state.prediction.PredictionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle,
    val productStockManager: ProductStockManager,
    val predictionEngine: ConsumptionPredictionEngine
): ViewModel() {
    private val productId = checkNotNull(savedStateHandle.get<Long>("productId"))

    val productDetails = repository.getProductWithMultipliersByIdFlow(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val predictionState: StateFlow<PredictionUiState> = productDetails
        .filterNotNull()
        .transform {_ ->
            emit(PredictionUiState.Loading)

            val result = predictionEngine.predict(productId)
            if (result != null) {
                emit(PredictionUiState.Success(result))
            } else {
                emit(PredictionUiState.NotEnoughData)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PredictionUiState.Loading
        )

    fun deleteProduct() {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun onReasonSelected(reason: ChangeReason) {
        viewModelScope.launch {
            productStockManager.StockAdjustmentDialogOnReasonSelected(reason)
        }
    }
}