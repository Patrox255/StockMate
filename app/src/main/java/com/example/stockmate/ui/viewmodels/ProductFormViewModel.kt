package com.example.stockmate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dtos.AddProductFormState
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.mappers.toMultipliers
import com.example.stockmate.data.mappers.toProduct
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.FormImageTracker
import com.example.stockmate.data.util.img.ImageStorage
import com.example.stockmate.data.validationUtil.FormValidationUtil
import com.example.stockmate.data.validationUtil.FormValidator
import com.example.stockmate.data.validationUtil.ValidatableItem
import com.example.stockmate.data.validationUtil.ValidatorGeneratorData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductFormUiEvent {
    data class ProductNotFound(val message: String) : ProductFormUiEvent()
    data object NavigateBack : ProductFormUiEvent()
}

@HiltViewModel
class ProductFormViewModel @Inject constructor (
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle,
    private val imageStorage: ImageStorage,
    private val formImageTracker: FormImageTracker
): ViewModel() {
    enum class AddProductFormField {
        NAME,
        UNIT,
        TARGET_STOCK,
    }
    enum class MultiplierFormField {
        NAME,
        VALUE,
    }
    private val productValidator = FormValidator(
        enumClass = AddProductFormField::class.java,
        validationFuns = mapOf(
            AddProductFormField.NAME to listOf(
                FormValidationUtil.stringNotBlank(ValidatorGeneratorData(
                    customErrorMessage = "Product name can't be empty!"
                ))
            ),
            AddProductFormField.UNIT to listOf(
                FormValidationUtil.stringNotBlank(ValidatorGeneratorData(
                    customErrorMessage = "Product unit can't be empty!"
                ))
            ),
            AddProductFormField.TARGET_STOCK to listOf(
                FormValidationUtil.validateFloat()
            ),
        )
    )
    val productErrors = productValidator.errors
    val productGlobalError = productValidator.error
    val onNameChanged = productValidator.onFormFieldChangedGenerator(AddProductFormField.NAME,
        {newName -> _formState.update { it.copy(name=newName) }})

    val onUnitChanged = productValidator.onFormFieldChangedGenerator(AddProductFormField.UNIT,
        {newUnit -> _formState.update { it.copy(unit=newUnit) }})

    val onTargetStockChanged = productValidator.onFormFieldChangedGenerator(AddProductFormField.TARGET_STOCK,
        {newTargetStock -> _formState.update { it.copy(targetStock=newTargetStock) }})


    private val multiplierValidator = FormValidator(
        enumClass = MultiplierFormField::class.java,
        validationFuns = mapOf(
            MultiplierFormField.NAME to listOf(
                FormValidationUtil.stringNotBlank(ValidatorGeneratorData(
                    customErrorMessage = "Multiplier name can't be empty!"
                ))
            ),
            MultiplierFormField.VALUE to listOf(
                FormValidationUtil.validateFloat(ValidatorGeneratorData(
                    customErrorMessage = "Multiplier value must be a valid number!"
                )),
                FormValidationUtil.floatGreaterOrEqualThan(ValidatorGeneratorData(
                    customErrorMessage = "Multiplier value must be greater than or equal to 0!"),
                    threshold = 0f
                )
            )
        )
    )
    val multiplierErrors = multiplierValidator.errors
    fun onMultiplierFieldChanged(multiplierId: String, field: MultiplierFormField, newValue: String) {
        _formState.update { state ->
            val res = state.copy(multipliers = state.multipliers.map {m ->
                if (m.localId != multiplierId) m
                else when (field) {
                    MultiplierFormField.NAME -> m.copy(name = newValue)
                    MultiplierFormField.VALUE -> m.copy(value = newValue)
                }
            })
            multiplierValidator.validateField(itemId = multiplierId, field = field, value = newValue)
            res
        }
    }

    private val _formState = MutableStateFlow(AddProductFormState())
    private var initialState = AddProductFormState()
    // This is used to store the initial multipliers data when editing a product, so we can check
    // for unsaved changes in the multipliers section. We only store the name, value and sortOrder of each multiplier,
    // since the localId is generated on the fly and will always be different.
    private var initialMultipliersData: List<Triple<String, String, Int>> = emptyList()
    private val _isLoadingExistingData = MutableStateFlow(false)
    private val _uiEvent = MutableSharedFlow<ProductFormUiEvent>()

    val productId: Long? = savedStateHandle.get<Long>("productId")
    val isEditMode: Boolean = productId != null
    val isLoadingExistingData: StateFlow<Boolean> = _isLoadingExistingData.asStateFlow()
    val formState: StateFlow<AddProductFormState> = _formState.asStateFlow()
    val uiEvent = _uiEvent.asSharedFlow()
    val hasUnsavedChanges: Boolean
        get() = if (isEditMode && _isLoadingExistingData.value) {
            false
        } else {
            val productFormChanged = _formState.value != initialState
            val currentMultipliersComparisionData = multiplierFormStateListToComparisionData(
                formState.value.multipliers)
            val multipliersChanged = currentMultipliersComparisionData != initialMultipliersData

            productFormChanged || multipliersChanged
        }


    init {
        if (isEditMode) {
            loadProductData(productId!!)
        }
    }

    private fun loadProductData(id: Long) {
        viewModelScope.launch {
            _isLoadingExistingData.value = false
            val productWithMultipliers = productRepository.getProductWithMultipliersById(id)

            if (productWithMultipliers != null) {
                _formState.value = AddProductFormState(
                    name = productWithMultipliers.product.name,
                    unit = productWithMultipliers.product.unit,
                    targetStock = productWithMultipliers.product.targetStock.toString(),
                    currentStock = productWithMultipliers.product.currentStock.toString(),
                    imagePath = productWithMultipliers.product.imageUrl,
                    multipliers = productWithMultipliers.multipliers.map { multiplier ->
                        MultiplierFormState(
                            databaseId = multiplier.id,
                            name = multiplier.name,
                            value = multiplier.value.toString(),
                            sortOrder = multiplier.sortOrder
                        )
                    }
                )

                initialMultipliersData = multiplierFormStateListToComparisionData(
                    _formState.value.multipliers
                )

                formImageTracker.init(productWithMultipliers.product.imageUrl)
            } else {
                _uiEvent.emit(ProductFormUiEvent.ProductNotFound("Error: Product with ID $id not found. Therefore the form cannot be loaded."))
            }

            _isLoadingExistingData.value = false
            initialState = _formState.value
        }
    }

    private fun multiplierFormStateListToComparisionData(multipliers: List<MultiplierFormState>): List<Triple<String, String, Int>> {
        return multipliers.mapIndexed { index, multiplier ->
            Triple(multiplier.name, multiplier.value, index)
        }
    }

    fun onImageChanged(newPath: String) {
        formImageTracker.onImageChanged(newPath)
        _formState.update {
            it.copy(imagePath = newPath)
        }
    }

    fun saveProduct() {
        val current = _formState.value

        val isProductFormValid = productValidator.validateSingleForm(
            mapOf(
                AddProductFormField.NAME to current.name,
                AddProductFormField.TARGET_STOCK to current.targetStock,
                AddProductFormField.UNIT to current.unit
            )
        )
        val areMultipliersValid = validateMultipliers(current)
        if (!isProductFormValid || !areMultipliersValid) {
            if (!areMultipliersValid) {
                productValidator.setGlobalError("Please fix the errors in the multipliers section.")
            }
            return
        }


        viewModelScope.launch {
            val newProduct = current.toProduct(
                id = productId ?: 0L,
                currentStock = current.currentStock.toFloatOrNull() ?: 0f
            )

            if (isEditMode) {
                productRepository.updateProductWithMultipliers(
                    product = newProduct,
                    multipliers =  current.multipliers.toMultipliers(newProduct.id)
                )
            } else {
                productRepository.insertProductWithMultipliers(
                    product = newProduct,
                    multipliers =  current.multipliers.toMultipliers(newProduct.id)
                )
            }
            formImageTracker.markAsSaved()
            formImageTracker.cleanUp()

            _uiEvent.emit(ProductFormUiEvent.NavigateBack)
        }
    }

    private fun validateMultipliers(current: AddProductFormState): Boolean = multiplierValidator.validateItems(
        current.multipliers.map { multiplier ->
            ValidatableItem(
                id = multiplier.localId,
                fields = mapOf(
                    MultiplierFormField.NAME to multiplier.name,
                    MultiplierFormField.VALUE to multiplier.value
                )
            )
        }
    )

    fun getProductWithMultipliersForPreview(): ProductWithMultipliers {
        val current = _formState.value
        val product = current.toProduct(
            id = productId ?: 0L,
            currentStock = 0f
        )
        val multipliers = current.multipliers

        return ProductWithMultipliers(
            product = product,
            multipliers = multipliers.toMultipliers(product.id)
        )
    }

    private fun changeMultipliers(changeFn: (AddProductFormState) -> List<MultiplierFormState>) {
        _formState.update { state ->
            val updatedMultipliers = changeFn(state)
            state.copy(multipliers = updatedMultipliers)
        }
        validateMultipliers(_formState.value)
    }

    fun addEmptyMultiplier() {
        changeMultipliers { state ->
            val newMultiplier = MultiplierFormState()
            state.multipliers + newMultiplier
        }
    }

    fun updateMultiplier(localId: String, name: String, value: String) {
        onMultiplierFieldChanged(localId, MultiplierFormField.NAME, name)
        onMultiplierFieldChanged(localId, MultiplierFormField.VALUE, value)
    }

    fun removeMultiplier(localId: String) {
        changeMultipliers { state ->
            state.multipliers.filter { multiplier ->
                multiplier.localId != localId
            }
        }
    }

    fun moveMultiplier(fromIndex: Int, toIndex: Int) {
        changeMultipliers { state ->
            val multipliers = state.multipliers.toMutableList()
            val multiplierToMove = multipliers.removeAt(fromIndex)
            multipliers.add(toIndex, multiplierToMove)
            multipliers
        }
    }

    override fun onCleared() {
        super.onCleared()

        formImageTracker.cleanUp()
    }
}