package com.example.stockmate.data.util.formatting

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Double.toCleanString(): String {
    val symbols = DecimalFormatSymbols(Locale.getDefault())
    val format = DecimalFormat("0.####", symbols)
    return format.format(this)
}

fun Float.toCleanString(): String = this.toDouble().toCleanString()

fun String.toFloatOrZero(): Float {
    return this.replace(",", ".").toFloatOrNull() ?: 0f
}

fun String.toFloatOrNullWithCommaSupport(): Float? {
    return this.replace(",", ".").toFloatOrNull()
}