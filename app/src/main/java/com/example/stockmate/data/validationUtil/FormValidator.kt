package com.example.stockmate.data.validationUtil

import android.util.Log
import androidx.compose.ui.text.MultiParagraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.set

typealias formErrors<fieldsType> = Map<FieldErrorKey<fieldsType>, MutableList<String>>
typealias formErrorsSingleForm<fieldsType> = Map<fieldsType, MutableList<String>>
typealias ValidationRule = (Any?) -> String?
typealias validationFuns<T> = Map<T, List<ValidationRule>>

data class FieldErrorKey<TField>(
    // Should be null in case of not handling a list of items but a single form
    val itemId: String? = null,
    val field: TField
)

data class ValidatableItem<TField>(
    val id: String,
    val fields: Map<TField, Any?>
)

class FormValidator<TField : Enum<TField>>(
    private val enumClass: Class<TField>,
    private val validationFuns: validationFuns<TField>,
    private val defaultErrorMessage: String = "Invalid product data provided. Please try again according to the guidelines!"
) {
    private val _errors = MutableStateFlow<formErrors<TField>>(emptyMap())
    private val _error = MutableStateFlow<String?>(null)

    val errors: StateFlow<formErrors<TField>> = _errors.asStateFlow()
    val error = _error.asStateFlow()

    private fun validateField(field: TField, value: Any?): List<String> {
        return validationFuns[field]
            ?.mapNotNull { rule -> rule(value) }
            ?: emptyList()
    }

    fun validateField(
        itemId: String?,
        field: TField,
        value: Any?
    ) {
        _errors.update { currentErrors ->
            val errors = validateField(field, value)
            val key = FieldErrorKey(itemId, field)

            if (errors.isEmpty()) {
                currentErrors - key
            } else {
                currentErrors + (key to errors.toMutableList())
            }
        }
    }

    fun onFormFieldChangedGenerator(
        field: TField, updateState: (String) -> Unit, itemId: String? = null
    ): (String) -> Unit {
        return { value ->
            updateState(value)
            validateField(itemId, field, value)
        }
    }

    private fun validateItemFields(
        fields: Map<TField, Any?>,
        errorMsgGenerator: (field: TField) -> String = { "Missing field ${it}" },
        fieldErrorKeyGenerator: (field: TField) -> FieldErrorKey<TField> = { FieldErrorKey(null, it) },
    ): Map<FieldErrorKey<TField>, MutableList<String>> {
        val errors = mutableMapOf<FieldErrorKey<TField>, MutableList<String>>()

        enumClass.enumConstants.forEach { entry ->
            if (!fields.containsKey(entry))
                throw Error(errorMsgGenerator(entry))
            val value = fields[entry]
            val fieldErrors = validateField(entry, value)
            if (fieldErrors.isNotEmpty()) {
                errors[fieldErrorKeyGenerator(entry)] = fieldErrors.toMutableList()
            }
        }

        return errors
    }

    fun validateItems(
        items: List<ValidatableItem<TField>>
    ): Boolean {
        _error.value = null
        val errors = mutableMapOf<FieldErrorKey<TField>, MutableList<String>>()

        items.forEach { item ->
            val itemFields = item.fields
            errors.putAll(
                validateItemFields(
                    itemFields,
                    errorMsgGenerator = { "Missing field ${it} in item with id ${item.id}" },
                    fieldErrorKeyGenerator = { FieldErrorKey(item.id, it) }
                )
            )
            Log.d("FormValidator", "Validated item with id ${item.id}, errors: ${errors.filterKeys { it.itemId == item.id }}, itemFields: $itemFields")
        }

        _errors.value = errors

        if (errors.isNotEmpty()) {
            _error.value = defaultErrorMessage
            return false
        }

        return true
    }

    fun validateSingleForm(fields: Map<TField, Any?>): Boolean {
        _error.value = null
        val errors = validateItemFields(fields)

        _errors.value = errors

        if (errors.isNotEmpty()) {
            _error.value = defaultErrorMessage
            return false
        }

        return true
    }

    fun setGlobalError(errorMessage: String) {
        _error.value = errorMessage
    }
}