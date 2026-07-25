package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val productId = checkNotNull(savedStateHandle.get<Long>("productId"))

    val productDetails = repository.getProductWithMultipliersByIdFlow(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun changeStock(product: Product, amount: Float, reason: ChangeReason) {
        viewModelScope.launch {
            repository.changeStock(product, amount, reason)
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }
}