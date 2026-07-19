package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.entity.StockLog
import com.example.stockmate.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor (
    private val productRepository: ProductRepository
): ViewModel() {
    var allProducts: StateFlow<List<ProductWithMultipliers>> = productRepository.getAllProductsWithMultipliers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun changeStock(product: Product, amount: Float, reason: ChangeReason) {
        viewModelScope.launch {
            productRepository.changeStock(product, amount, reason)
        }
    }
}