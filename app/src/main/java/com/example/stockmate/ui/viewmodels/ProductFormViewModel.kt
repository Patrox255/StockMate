package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dtos.AddProductFormState
import com.example.stockmate.data.dtos.MultiplierField
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.FormImageTracker
import com.example.stockmate.data.util.ImageStorage
import com.example.stockmate.data.validationUtil.FormValidationUtil
import com.example.stockmate.data.validationUtil.FormValidator
import com.example.stockmate.data.validationUtil.ValidatorGeneratorData
import com.example.stockmate.ui.viewmodels.productMultiplier.MultipliersFormManager
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
        PACKAGE_SIZE
    }
    private val formValidator = FormValidator(
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
            AddProductFormField.PACKAGE_SIZE to listOf(
                FormValidationUtil.validateFloat()

            )
        )
    )

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
    val errors = formValidator.errors
    val error = formValidator.error
    val uiEvent = _uiEvent.asSharedFlow()
    val hasUnsavedChanges: Boolean
        get() = if (isEditMode && _isLoadingExistingData.value) {
            false
        } else {
            val productFormChanged = _formState.value != initialState
            val currentMultipliersComparisionData = multiplierFormStateListToComparisionData(
                multipliersManager.multipliers.value)
            val multipliersChanged = currentMultipliersComparisionData != initialMultipliersData

            productFormChanged || multipliersChanged
        }
    val multipliersManager = MultipliersFormManager(
        validationRules = mapOf(
            MultiplierField.NAME to listOf(
                FormValidationUtil.stringNotBlank(ValidatorGeneratorData(
                    customErrorMessage = "Multiplier name can't be empty!"
                ))
            ),
            MultiplierField.VALUE to listOf(
                FormValidationUtil.validateFloat(ValidatorGeneratorData(
                    customErrorMessage = "Multiplier value must be a valid number!"
                ))
            )
        )
    )

    val onNameChanged = formValidator.onFormFieldChangedGenerator(AddProductFormField.NAME) { newName ->
        _formState.update { it.copy(name = newName) }
    }

    val onUnitChanged = formValidator.onFormFieldChangedGenerator(AddProductFormField.UNIT) { newUnit ->
        _formState.update { it.copy(unit = newUnit) }
    }

    val onTargetStockChanged = formValidator.onFormFieldChangedGenerator(AddProductFormField.TARGET_STOCK) { newStock ->
        _formState.update { it.copy(targetStock = newStock) }
    }

    val onPackageSizeChanged = formValidator.onFormFieldChangedGenerator(AddProductFormField.PACKAGE_SIZE) { newSize ->
        _formState.update { it.copy(packageSize = newSize) }
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
                    packageSize = productWithMultipliers.product.packageSize.toString(),
                    currentStock = productWithMultipliers.product.currentStock.toString(),
                    imagePath = productWithMultipliers.product.imageUrl
                )
                multipliersManager.loadExistingMultipliers(productWithMultipliers.multipliers)

                initialMultipliersData = multiplierFormStateListToComparisionData(
                    multipliersManager.multipliers.value)

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

        val isFormValid = formValidator.validateBeforeSubmit(
            mapOf(
                AddProductFormField.NAME to current.name,
                AddProductFormField.PACKAGE_SIZE to current.packageSize,
                AddProductFormField.TARGET_STOCK to current.targetStock,
                AddProductFormField.UNIT to current.unit
            )
        )
        val areMultipliersValid = multipliersManager.areAllMultipliersValid()
        if (!isFormValid || !areMultipliersValid) {
            if (!areMultipliersValid) {
                formValidator.setGlobalError("Please fix the errors in the multipliers section.")
            }
            return
        }


        viewModelScope.launch {

            val newProduct = Product(
                id = productId ?: 0L,
                name = current.name,
                unit = current.unit,
                currentStock = 0f,
                targetStock = current.targetStock.toFloat(),
                packageSize = current.packageSize.toFloat(),
                imageUrl = current.imagePath
            )

            if (isEditMode) {
                productRepository.updateProductWithMultipliers(
                    product = newProduct,
                    multipliers = multipliersManager.getProductMultipliers()
                )
            } else {
                productRepository.insertProductWithMultipliers(
                    product = newProduct,
                    multipliers = multipliersManager.getProductMultipliers()
                )
            }
            formImageTracker.markAsSaved()
            formImageTracker.cleanUp()

            _uiEvent.emit(ProductFormUiEvent.NavigateBack)
        }
    }

    override fun onCleared() {
        super.onCleared()

        formImageTracker.cleanUp()
    }
}