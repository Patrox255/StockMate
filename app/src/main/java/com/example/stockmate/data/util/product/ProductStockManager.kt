package com.example.stockmate.data.util.product

import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.prediction.ConsumptionPredictionEngine
import com.example.stockmate.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProductStockManager @Inject constructor(
    private val predictionEngine: ConsumptionPredictionEngine,
    private val repository: ProductRepository
) {
    private val _showDialog: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _pendingDifference: MutableStateFlow<Float> = MutableStateFlow(0f)
    private val _pendingProduct = MutableStateFlow<Product?>(null)

    val showDialog = _showDialog.asStateFlow()
    val pendingDifference: StateFlow<Float> = _pendingDifference.asStateFlow()

    fun StockAdjustmentControlsOnPlusClick(product: Product, selectedMultiplier: ProductMultiplier?) {
        _pendingProduct.value = product
        _pendingDifference.value = selectedMultiplier?.value ?: 0f
        _showDialog.value = true
    }

    fun StockAdjustmentControlsOnMinusClick(product: Product, selectedMultiplier: ProductMultiplier?) {
        _pendingProduct.value = product
        _pendingDifference.value = -(selectedMultiplier?.value ?: 0f)
        _showDialog.value = true
    }

    suspend fun changeStock(product: Product, amount: Float, reason: ChangeReason) {
        predictionEngine.clearCacheForProduct(product.id)
        repository.changeStock(product, amount, reason)
    }

    suspend fun StockAdjustmentDialogOnReasonSelected(reason: ChangeReason) {
        val product = _pendingProduct.value
        if (product != null) {
            changeStock(product, _pendingDifference.value, reason)
        }
        _showDialog.value = false
        _pendingProduct.value = null
    }

    fun StockAdjustmentDialogOnDismiss() {
        _showDialog.value = false
        _pendingProduct.value = null
    }

    fun showDialogUpdate(value: Boolean) {
        _showDialog.value = value
    }

    fun pendingDifferenceUpdate(value: Float) {
        _pendingDifference.value = value
    }
}