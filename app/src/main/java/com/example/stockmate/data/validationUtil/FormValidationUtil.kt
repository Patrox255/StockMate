package com.example.stockmate.data.validationUtil

import android.util.Log

typealias ValidatorGenerator = (genData: ValidatorGeneratorData) -> ((String) -> String?)

data class ValidatorGeneratorData(
    val customErrorMessage: String? = null
)

object FormValidationUtil {
    val floatRegex = """^-?\d+(\.\d+)?$""".toRegex()

    // This function helps to create rules for specific types, throwing an exception if the value
    // is not of the expected type, which then has to be handled by the rule.
    private inline fun <reified T> typedRule(
        typeErrorMessage: String = "Invalid value type provided for the field.",
        crossinline initialStringTransformation: (String) -> T? = { value -> value as? T },
        crossinline rule: (T?) -> String?,
    ) : ValidationRule = { value ->
        when {
            value == null -> rule(null)
            value is T -> rule(value)
            value is String -> {
                val transformedVal = initialStringTransformation(value)
                if (transformedVal is T) {
                    rule(transformedVal)
                } else {
                    typeErrorMessage
                }
            }
            else -> typeErrorMessage
        }
    }
    fun validateFloat(data: ValidatorGeneratorData = ValidatorGeneratorData()): ValidationRule
    {
        val errorMsg = data.customErrorMessage ?: "Must be a valid decimal number (e.g., 12.34)"
        return typedRule<String>(errorMsg) { value ->
            if (value == null || !value.matches(floatRegex)) {
                errorMsg
            } else {
                null
            }
        }
    }

    fun stringNotBlank(data: ValidatorGeneratorData = ValidatorGeneratorData()): ValidationRule
    {
        val errorMsg = data.customErrorMessage ?: "Can't be empty!"
        return typedRule<String>(errorMsg) { value ->
            if (value == null || value.isBlank()) {
                errorMsg
            } else {
                null
            }
        }
    }

    fun objectNotNull(data: ValidatorGeneratorData = ValidatorGeneratorData()): ValidationRule
    {
        val errorMsg = data.customErrorMessage ?: "Must be selected!"
        return typedRule<Any?>(errorMsg) { value ->
            if (value == null) {
                errorMsg
            } else {
                null
            }
        }
    }

    fun doubleGreaterThan(data: ValidatorGeneratorData = ValidatorGeneratorData(), threshold: Double): ValidationRule {
        val errorMsg = data.customErrorMessage ?: "Must be greater than $threshold"
        return typedRule<Double>(
            typeErrorMessage = errorMsg,
            initialStringTransformation = { value -> value.replace(",", ".").toDoubleOrNull() }
        ) { value ->
            if (value == null || value <= threshold) {
                errorMsg
            } else {
                null
            }
        }
    }

    fun floatGreaterOrEqualThan(data: ValidatorGeneratorData = ValidatorGeneratorData(), threshold: Float): ValidationRule {
        val errorMsg = data.customErrorMessage ?: "Must be greater than $threshold"
        return typedRule<Float>(
            typeErrorMessage = errorMsg,
            initialStringTransformation = { value -> value.replace(",", ".").toFloatOrNull() }
        ) { value ->
            if (value == null || value < threshold) {
                errorMsg
            } else {
                null
            }
        }
    }
}