package com.example.stockmate.data.dtos

data class AddProductFormState(
    val name: String = "",
    val unit: String = "units",
    val targetStock: String = "",
    val packageSize: String = "1"
)
