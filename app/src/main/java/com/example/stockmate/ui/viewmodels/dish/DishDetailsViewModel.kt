package com.example.stockmate.ui.viewmodels.dish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.DishWithIngredients
import com.example.stockmate.data.entity.IngredientWithProductAndMultiplier
import com.example.stockmate.data.repository.DishRepository
import com.example.stockmate.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DishDetailsUiState {
    object Loading: DishDetailsUiState
    data class Success(
        val dishWithIngredients: DishWithIngredients
    ): DishDetailsUiState
    data class Error(
        val message: String
    ): DishDetailsUiState
}

sealed interface DishDetailsUiEvent {
    data class ShowToast(val message: String): DishDetailsUiEvent
}

@HiltViewModel
class DishDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository
): ViewModel() {
    private val _showCookDialog = MutableStateFlow(false)
    private val _uiEvent = MutableSharedFlow<DishDetailsUiEvent>()

    val dishId = savedStateHandle.get<Long>("dishId") ?: 0L
    val uiState: StateFlow<DishDetailsUiState> = dishRepository
        .getDishWithIngredientsFlow(dishId)
        .map { dishWithIngredients ->
            if (dishWithIngredients != null) {
                DishDetailsUiState.Success(dishWithIngredients)
            } else {
                DishDetailsUiState.Error("Dish not found")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DishDetailsUiState.Loading
        )
    val showCookDialog = _showCookDialog.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onCookClick() {
        _showCookDialog.value = true
    }
    fun onDismissCookDialog() {
        _showCookDialog.value = false
    }

    fun confirmCook(data: DishWithIngredients) {
        viewModelScope.launch {
            val dish = data.dish
            val ingredients = data.ingredients

            _showCookDialog.value = false
            try {
                ingredients.forEach { item ->
                    val requiredQuantity =
                        (item.ingredient.amount * item.multiplier.value).toFloat()
                    productRepository.changeStock(
                        product = item.product,
                        amount = -requiredQuantity,
                        reason = ChangeReason.CONSUMED,
                        relatedDishId = dishId,
                        relatedDishName = dish.name
                    )
                }
            } catch (e: Exception) {
                _uiEvent.emit(DishDetailsUiEvent.ShowToast("Error while cooking ${dish.name}!"))
                return@launch
            }

            _uiEvent.emit(DishDetailsUiEvent.ShowToast("Dish cooked successfully!"))
        }
    }
}