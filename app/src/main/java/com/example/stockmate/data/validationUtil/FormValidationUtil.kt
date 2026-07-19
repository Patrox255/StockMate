package com.example.stockmate.data.validationUtil

typealias ValidatorGenerator = (genData: ValidatorGeneratorData) -> ((String) -> String?)

data class ValidatorGeneratorData(
    val customErrorMessage: String? = null
)

object FormValidationUtil {
    val floatRegex = """^-?\d+(\.\d+)?$""".toRegex()

    fun validateFloat(data: ValidatorGeneratorData = ValidatorGeneratorData()): (String) -> String?
    {
        return { value ->
            if (!value.matches(floatRegex)) {
                data.customErrorMessage ?: "Must be a valid decimal number (e.g., 12.34)"
            } else {
                null
            }
        }
    }

    fun stringNotBlank(data: ValidatorGeneratorData = ValidatorGeneratorData()): (String) -> String?
    {
        return { value ->
            if (value.isBlank()) {
                data.customErrorMessage ?: "Can't be empty!"
            } else {
                null
            }
        }
    }
}