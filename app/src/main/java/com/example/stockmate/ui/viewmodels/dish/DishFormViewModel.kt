package com.example.stockmate.ui.viewmodels.dish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.mappers.toDraftDish
import com.example.stockmate.data.repository.DishRepository
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import com.example.stockmate.data.util.selection.PaginatedSelectionManager
import com.example.stockmate.data.validationUtil.FieldErrorKey
import com.example.stockmate.data.validationUtil.FormValidationUtil
import com.example.stockmate.data.validationUtil.FormValidator
import com.example.stockmate.data.validationUtil.ValidatorGeneratorData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DraftIngredient(
    val localId: String = UUID.randomUUID().toString(),
    val databaseId: Long? = null,
    val product: Product? = null,
    val multiplier: ProductMultiplier? = null,
    val amount: Double = 0.0
)

data class DraftDish(
    val name: String = "New dish",
    val ingredients: List<DraftIngredient> = emptyList(),
    val description: String = ""
)

sealed class DishFormUiEvent {
    data class DishNotFound(val message: String) : DishFormUiEvent()
    data object NavigateBack : DishFormUiEvent()
}

@HiltViewModel
class DishFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    enum class AddDishFormField {
        NAME,
        DESCRIPTION,
    }
    enum class AddIngredientFormField {
        PRODUCT,
        MULTIPLIER,
        AMOUNT
    }

    private val dishFormValidator = FormValidator(
        enumClass = AddDishFormField::class.java,
        validationFuns = mapOf(
            AddDishFormField.NAME to listOf(
                FormValidationUtil.stringNotBlank(
                    ValidatorGeneratorData(
                        customErrorMessage = "Dish name can't be empty!"
                    )
                )
            ),
            AddDishFormField.DESCRIPTION to listOf(
                FormValidationUtil.stringNotBlank(
                    ValidatorGeneratorData(
                        customErrorMessage = "Dish description can't be empty!"
                    )
                )
            )
        ),
        defaultErrorMessage = "Invalid dish data provided. Please try again according to the guidelines!"
    )
    val dishGlobalError = dishFormValidator.error
    val dishErrors = dishFormValidator.errors

    private val ingredientFormValidator = FormValidator(
        enumClass = AddIngredientFormField::class.java,
        validationFuns = mapOf(
            AddIngredientFormField.PRODUCT to listOf(
                FormValidationUtil.objectNotNull(
                    ValidatorGeneratorData(
                        customErrorMessage = "Product must be selected!"
                    )
                )
            ),
            AddIngredientFormField.MULTIPLIER to listOf(
                FormValidationUtil.objectNotNull(
                    ValidatorGeneratorData(
                        customErrorMessage = "Multiplier must be selected!"
                    )
                )
            ),
            AddIngredientFormField.AMOUNT to listOf(
                FormValidationUtil.doubleGreaterThan(
                    ValidatorGeneratorData(
                        customErrorMessage = "Amount must be greater than 0!"
                    ),
                    threshold = 0.0
                )
            )
        )
    )
    val ingredientErrors = ingredientFormValidator.errors

    private val _dish = MutableStateFlow(DraftDish(name = "", ingredients = emptyList()))
    private val _isLoadingExistingData = MutableStateFlow(false)
    private val _uiEvent = MutableSharedFlow<DishFormUiEvent>()
    private val _productsSearchEngine = SearchSortFilterEngine<Product>(
        initialFilterGroups = emptyList(),
        searchMatcher = { item, query ->
            item.name.contains(query, ignoreCase = true)
        },
    )

    private var initialState = DraftDish()

    val dishId: Long? = savedStateHandle.get<Long>("dishId")
    val isEditMode: Boolean = dishId != null
    val productsPaginatedSelectionManager = PaginatedSelectionManager(
        searchEngine = _productsSearchEngine,
        scope = viewModelScope,
        pageSize = 5
    )
    val productsPaginatedSelectionState = productsPaginatedSelectionManager.state
    val dish = _dish.asStateFlow()
    val isLoadingExistingData = _isLoadingExistingData.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()
    val hasUnsavedChanges: Boolean
        get() = if (isEditMode && _isLoadingExistingData.value)
            false
        else _dish.value != initialState

    init {
        productsPaginatedSelectionManager.initialize(productRepository.getAllProductsFlow())

        if (isEditMode) {
            dishId?.let { loadDishData(it) }
        }
    }

    val onNameChanged = dishFormValidator.onFormFieldChangedGenerator(
        field = AddDishFormField.NAME,
        updateState = {newName ->
            _dish.value = _dish.value.copy(name = newName)
        }
    )
    val onDescriptionChanged = dishFormValidator.onFormFieldChangedGenerator(
        field = AddDishFormField.DESCRIPTION,
        updateState = { newDescription ->
            _dish.value = _dish.value.copy(description = newDescription)
        }
    )
    fun removeIngredient(ingredient: DraftIngredient) {
        _dish.value = _dish.value.copy(ingredients = _dish.value.ingredients.filter { it != ingredient })
    }
    fun addEmptyIngredient() {
        _dish.value = _dish.value.copy(ingredients = _dish.value.ingredients + DraftIngredient())
    }
    fun updateIngredient(updatedIngredient: DraftIngredient) {
        _dish.value = _dish.value.copy(
            ingredients = _dish.value.ingredients.map { ingredient ->
                if (ingredient.localId == updatedIngredient.localId) {
                    updatedIngredient
                } else {
                    ingredient
                }
            }
        )
    }
    val getIngredientFieldErrors: (localId: String, field: AddIngredientFormField) -> MutableList<String> = { localId, field ->
        val ingredient = _dish.value.ingredients.find { it.localId == localId }
        if (ingredient == null) {
            mutableListOf()
        } else {
            ingredientErrors.value[FieldErrorKey(
                field = field,
                itemId = localId
            )] ?: mutableListOf()
        }
    }
    fun saveDish() {
        val isDishValid = dishFormValidator.validateSingleForm(
            mapOf(
                AddDishFormField.NAME to _dish.value.name,
                AddDishFormField.DESCRIPTION to _dish.value.description
            )
        )
        if (!isDishValid)
            return

        viewModelScope.launch {
            _uiEvent.emit(DishFormUiEvent.NavigateBack)
        }
    }

    private fun loadDishData(dishId: Long) {
        viewModelScope.launch {
            _isLoadingExistingData.value = true
            val dishWithIngredients = dishRepository.getDishWithIngredients(dishId)

            if (dishWithIngredients == null) {
                _uiEvent.emit(DishFormUiEvent.DishNotFound("Dish with id $dishId not found"))
                _isLoadingExistingData.value = false
                return@launch
            }

            _dish.value = dishWithIngredients.toDraftDish()
            initialState = _dish.value
            _isLoadingExistingData.value = false
        }
    }
}