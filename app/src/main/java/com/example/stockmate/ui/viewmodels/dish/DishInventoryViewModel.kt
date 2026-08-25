package com.example.stockmate.ui.viewmodels.dish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.DishWithIngredients
import com.example.stockmate.data.repository.DishRepository
import com.example.stockmate.data.util.pagination.SearchEnginePaginationManager
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DishInventoryViewModel @Inject constructor(
    private val dishRepository: DishRepository
): ViewModel() {
    companion object {
        const val PAGE_SIZE = 20
    }

    private val _dishSearchEngine = SearchSortFilterEngine<DishWithIngredients>(
      initialFilterGroups = emptyList(),
      searchMatcher = { dish, query ->
          val lowerCaseQuery = query.lowercase()
          dish.dish.name.lowercase().contains(lowerCaseQuery) ||
                  (dish.dish.description?.lowercase()?.contains(lowerCaseQuery) ?: false) ||
                  dish.ingredients.any { ingredient ->
                      ingredient.product.name.lowercase().contains(lowerCaseQuery)
                  }
      },
    )

    val dishesPaginationManager = SearchEnginePaginationManager(
        searchEngine = _dishSearchEngine,
        scope = viewModelScope,
        pageSize = PAGE_SIZE
    )
    val dishesPaginationState = dishesPaginationManager.state

    init {
        dishesPaginationManager.initialize(
            dishRepository.getDishesWithIngredientsFlow()
        )
    }

    fun onSearchQueryChanged(
        query: String
    ) {
        dishesPaginationManager.updateSearchQuery(query)
    }
}