package com.example.stockmate.ui.viewmodels.productMultiplier

import com.example.stockmate.data.dtos.MultiplierField
import com.example.stockmate.data.dtos.MultiplierFormState
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.validationUtil.validationFuns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class MultipliersFormManager(
    private val validationRules: validationFuns<MultiplierField>
) {
    private val _multipliers = MutableStateFlow<List<MultiplierFormState>>(emptyList())

    val multipliers = _multipliers.asStateFlow()

    fun addEmptyMultiplier() {
        _multipliers.update {
            it + MultiplierFormState()
        }
    }
    private fun validateSingleField(field: MultiplierField, value: String): List<String> {
        return validationRules[field]?.mapNotNull { rule -> rule(value) } ?: emptyList()
    }
    private fun getMultiplierFieldsErrors(nameVal: String, valueVal: String): Map<MultiplierField, List<String>> {
        val newErrorsMap = mutableMapOf<MultiplierField, List<String>>()
        val nameErrors = validateSingleField(MultiplierField.NAME, nameVal)
        val valueErrors = validateSingleField(MultiplierField.VALUE, valueVal)
        if (nameErrors.isNotEmpty()) {
            newErrorsMap[MultiplierField.NAME] = nameErrors
        }
        if (valueErrors.isNotEmpty()) {
            newErrorsMap[MultiplierField.VALUE] = valueErrors
        }

        return newErrorsMap
    }
    fun updateMultiplier(id: String, name: String? = null, value: String? = null) {
        _multipliers.update { currentMultipliers ->
            currentMultipliers.map { multiplier ->
                if (multiplier.localId != id) {
                    multiplier
                } else {
                    val newErrorsMap = getMultiplierFieldsErrors(name ?: multiplier.name, value ?: multiplier.value)

                    multiplier.copy(
                        name = name ?: multiplier.name,
                        value = value ?: multiplier.value,
                        errors = newErrorsMap
                    )
                }
            }
        }
    }
    fun removeMultiplier(id: String) {
        _multipliers.update { currentMultipliers ->
            currentMultipliers.filter { it.localId != id }
        }
    }

    fun isMultiplierValid(
        multiplier: MultiplierFormState,
        omitUpdating: Boolean = false
    ): Boolean {
        val multiplierFieldErrors = getMultiplierFieldsErrors(multiplier.name, multiplier.value)
        if (!omitUpdating) {
            _multipliers.update { multipliers->
                multipliers.map { m ->
                    if (m.localId == multiplier.localId) {
                        m.copy(errors = multiplierFieldErrors)
                    } else {
                        m
                    }

                }
            }
        }
        return multiplierFieldErrors.isEmpty()
    }

    // This function automatically updates the errors for all multipliers in the list and returns true if all multipliers are valid, false otherwise.
    fun areAllMultipliersValid(): Boolean {
        return _multipliers.value.all { isMultiplierValid(it) }
    }

    fun getProductMultipliers(): List<ProductMultiplier> {
        return _multipliers.value.filter(this::isMultiplierValid).map { multiplier ->
            ProductMultiplier(
                productId = 0,
                name = multiplier.name,
                value = multiplier.value.toFloat()
            )
        }
    }

    fun loadExistingMultipliers(existingEntities: List<ProductMultiplier>) {
        _multipliers.value = existingEntities.map { entity ->
            MultiplierFormState(
                localId = UUID.randomUUID().toString(),
                name = entity.name,
                value = entity.value.toString()
            )
        }
    }
}