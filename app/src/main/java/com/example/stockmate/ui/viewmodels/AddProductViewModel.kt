package com.example.stockmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.data.dtos.AddProductFormState
import com.example.stockmate.data.dtos.MultiplierField
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.validationUtil.FormValidationUtil
import com.example.stockmate.data.validationUtil.FormValidator
import com.example.stockmate.data.validationUtil.ValidatorGeneratorData
import com.example.stockmate.ui.viewmodels.productMultiplier.MultipliersFormManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor (
    private val productRepository: ProductRepository
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
    private val initialState = AddProductFormState()

    val formState: StateFlow<AddProductFormState> = _formState.asStateFlow()
    val errors = formValidator.errors
    val error = formValidator.error
    val hasUnsavedChanges: Boolean
        get() = _formState.value != initialState
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

    fun saveProduct(onSuccess: () -> Unit) {
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
                id = 0,
                name = current.name,
                unit = current.unit,
                currentStock = 0f,
                targetStock = current.targetStock.toFloat(),
                packageSize = current.packageSize.toFloat()
            )

            productRepository.insertProductWithMultipliers(
                product = newProduct,
                multipliers = multipliersManager.getProductMultipliers()
            )
            onSuccess()
        }
    }
}