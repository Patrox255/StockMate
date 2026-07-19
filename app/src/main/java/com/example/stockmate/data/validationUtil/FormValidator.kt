package com.example.stockmate.data.validationUtil

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

typealias formErrors<fieldsType> = Map<fieldsType, MutableList<String>>
typealias validationFuns<T> = Map<T, List<(String) -> String?>>

class FormValidator<TField : Enum<TField>>(
    private val enumClass: Class<TField>,
    private val validationFuns: validationFuns<TField>,
    private val defaultErrorMessage: String = "Invalid product data provided. Please try again according to the guidelines!"
) {
    private val _errors = MutableStateFlow<formErrors<TField>>(emptyMap())
    private val _error = MutableStateFlow<String?>(null)

    val errors: StateFlow<formErrors<TField>> = _errors.asStateFlow()
    val error = _error.asStateFlow()

    fun updateErrorsBasedOnFieldNewVal(
        field: TField,
        value: String,
        errors: formErrors<TField>,
        validationFuns: validationFuns<TField>
    ): Map<TField, MutableList<String>> {
        val newErrorsMap = errors.toMutableMap()
        val fieldErrors = mutableListOf<String>()

        if (validationFuns.containsKey(field)) {
            val relatedFuns = validationFuns[field]!!
            relatedFuns.forEach { func ->
                val funcVal = func(value)
                if (funcVal != null)
                    fieldErrors.add(funcVal)
            }
        }

        if (fieldErrors.isNotEmpty()) {
            newErrorsMap[field] = fieldErrors
        } else {
            newErrorsMap.remove(field)
        }
        return newErrorsMap
    }

    fun onFormFieldChangedGenerator(
        field: TField, updateState: (String) -> Unit
    ): (String) -> Unit {
        return { value ->
            updateState(value)
            _errors.update { currentErrors ->
                updateErrorsBasedOnFieldNewVal(field, value, currentErrors, validationFuns)
            }
        }
    }

    fun validateBeforeSubmit(
        formFieldsToFormStateVals: Map<TField, String>
    ): Boolean {
        _error.value = null
        var errors: formErrors<TField> = mutableMapOf()

        enumClass.enumConstants.forEach { entry ->
            if (!formFieldsToFormStateVals.containsKey(entry))
                throw Error("Check your validateBeforeSubmit mapping argument!")
            val curFormStateFieldVal = formFieldsToFormStateVals.getValue(entry)
            errors = updateErrorsBasedOnFieldNewVal(
                entry,
                curFormStateFieldVal,
                errors,
                validationFuns
            )
        }

        _errors.value = errors
        if (errors.entries.isNotEmpty()) {
            _error.value = defaultErrorMessage
            return false
        }

        return true
    }

    fun setGlobalError(errorMessage: String) {
        _error.value = errorMessage
    }
}