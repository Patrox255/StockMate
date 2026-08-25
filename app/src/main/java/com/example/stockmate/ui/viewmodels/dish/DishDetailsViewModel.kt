package com.example.stockmate.ui.viewmodels.dish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.DishWithIngredients
import com.example.stockmate.data.repository.DishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

@HiltViewModel
class DishDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dishRepository: DishRepository
): ViewModel() {
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
}