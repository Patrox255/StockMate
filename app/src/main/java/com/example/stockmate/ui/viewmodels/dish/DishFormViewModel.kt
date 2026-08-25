package com.example.stockmate.ui.viewmodels.dish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.entity.DishWithIngredients
import com.example.stockmate.data.entity.IngredientWithProductAndMultiplier
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.mappers.toDishEntity
import com.example.stockmate.data.mappers.toDraftDish
import com.example.stockmate.data.mappers.toEntity
import com.example.stockmate.data.mappers.toIngredients
import com.example.stockmate.data.repository.DishRepository
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.FormImageTracker
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import com.example.stockmate.data.util.pagination.SearchEnginePaginationManager
import com.example.stockmate.data.validationUtil.FieldErrorKey
import com.example.stockmate.data.validationUtil.FormValidationUtil
import com.example.stockmate.data.validationUtil.FormValidator
import com.example.stockmate.data.validationUtil.ValidatableItem
import com.example.stockmate.data.validationUtil.ValidatorGeneratorData
import com.example.stockmate.data.validationUtil.formErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DraftIngredient(
    val localId: String = UUID.randomUUID().toString(),
    val databaseId: Long? = null,
    val product: Product? = null,
    val multiplier: ProductMultiplier? = null,
    // Thanks to validation this will always be a valid double,
    // but we keep it as a string for the UI to handle invalid input gracefully
    val amountStr: String = "0.0",
)

data class DraftDish(
    val name: String = "New dish",
    val ingredients: List<DraftIngredient> = emptyList(),
    val description: String = "",
    val imagePath: String? = null
)

sealed class DishFormUiEvent {
    data class DishNotFound(val message: String) : DishFormUiEvent()
    data object NavigateBack : DishFormUiEvent()
}

@HiltViewModel
class DishFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository,
    private val formImageTracker: FormImageTracker
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
    companion object {
        const val DEFAULT_PRODUCTS_SELECTION_PAGE_SIZE = 20
        const val DEFAULT_MULTIPLIERS_SELECTION_PAGE_SIZE = 20
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
    private val _multipliersSearchEngine = SearchSortFilterEngine<ProductMultiplier>(
        initialFilterGroups = emptyList(),
        searchMatcher = { item, query ->
            item.name.contains(query, ignoreCase = true)
        },
    )

    private var initialState = DraftDish()

    val dishId: Long? = savedStateHandle.get<Long>("dishId")
    val isEditMode: Boolean = dishId != null
    val productsSearchEnginePaginationManager = SearchEnginePaginationManager(
        searchEngine = _productsSearchEngine,
        scope = viewModelScope,
        pageSize = DEFAULT_PRODUCTS_SELECTION_PAGE_SIZE
    )
    val productsPaginatedSelectionState = productsSearchEnginePaginationManager.state
    val multipliersSearchEnginePaginationManager = SearchEnginePaginationManager(
        searchEngine = _multipliersSearchEngine,
        scope = viewModelScope,
        pageSize = DEFAULT_MULTIPLIERS_SELECTION_PAGE_SIZE
    )
    val multipliersPaginatedSelectionState = multipliersSearchEnginePaginationManager.state
    val dish = _dish.asStateFlow()
    val dishWithIngredientsForPreview = dish.map { dish ->
        DishWithIngredients(
            dish = dish.toDishEntity(dishId),
            ingredients = dish.ingredients.map { ingredient ->
                IngredientWithProductAndMultiplier(
                    ingredient = ingredient.toEntity(dishId),
                    product = ingredient.product ?: Product(
                        id = 0L,
                        name = "No product selected",
                        unit = "Unknown unit",
                        currentStock = 0f,
                        targetStock = 0f
                    ),
                    multiplier = ingredient.multiplier ?: ProductMultiplier(
                        id = 0L,
                        productId = ingredient.product?.id ?: 0L,
                        name = "No multiplier selected",
                        value = 0f,
                        sortOrder = 0
                    )
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DishWithIngredients(
            dish = DraftDish().toDishEntity(dishId),
            ingredients = emptyList()
        )
    )
    val isLoadingExistingData = _isLoadingExistingData.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()
    val hasUnsavedChanges: Boolean
        get() = if (isEditMode && _isLoadingExistingData.value)
            false
        else _dish.value != initialState

    init {
        productsSearchEnginePaginationManager.initialize(productRepository.getAllProductsFlow())

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
        validateIngredient(updatedIngredient)
    }
    private fun validateIngredient(ingredient: DraftIngredient): Boolean {
        val isValid = ingredientFormValidator.validateItems(
            listOf(
                ValidatableItem(
                    id = ingredient.localId,
                    fields = mapOf(
                        AddIngredientFormField.PRODUCT to ingredient.product,
                        AddIngredientFormField.MULTIPLIER to ingredient.multiplier,
                        AddIngredientFormField.AMOUNT to ingredient.amountStr
                    )
                )
            )
        )
        return isValid
    }
    fun ingredientFieldErrors(
        errors: formErrors<AddIngredientFormField>,
        localId: String,
        field: AddIngredientFormField
    ): MutableList<String> = errors[FieldErrorKey(itemId = localId, field = field)] ?: mutableListOf()
    fun saveDish() {
        val isDishValid = dishFormValidator.validateSingleForm(
            mapOf(
                AddDishFormField.NAME to _dish.value.name,
                AddDishFormField.DESCRIPTION to _dish.value.description
            )
        )
        val ingredientsValidationResults = _dish.value.ingredients.map {ingredient ->
            validateIngredient(ingredient)
        }
        val hasAnyIngredientErrors = ingredientsValidationResults.contains(false)
        if (!isDishValid || hasAnyIngredientErrors)
            return

        viewModelScope.launch {
            val dishEntity = _dish.value.toDishEntity(dishId)
            val ingredients = _dish.value.ingredients.toIngredients(dishId)

            if (isEditMode) {
                dishRepository.updateDishWithIngredients(dishEntity, ingredients)
            } else {
                dishRepository.insertDishWithIngredients(dishEntity, ingredients)
            }

            formImageTracker.markAsSaved()
            formImageTracker.cleanUp()
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
            formImageTracker.init(_dish.value.imagePath)
            _isLoadingExistingData.value = false
        }
    }
    fun selectIngredientProduct(
        localId: String,
        product: Product
    ) {
        val relatedIngredient = _dish.value.ingredients.find { it.localId == localId }
        if (relatedIngredient != null) {
            updateIngredient(
                relatedIngredient.copy(
                    product = product,
                    multiplier = null
                )
            )
        }

        multipliersSearchEnginePaginationManager.initialize(
            productRepository.getMultipliersByProductIdFlow(product.id)
        )
    }
    fun onImageChanged(newPath: String?) {
        formImageTracker.onImageChanged(newPath)
        _dish.value = _dish.value.copy(imagePath = newPath)
    }

    override fun onCleared() {
        super.onCleared()

        formImageTracker.cleanUp()
    }
}