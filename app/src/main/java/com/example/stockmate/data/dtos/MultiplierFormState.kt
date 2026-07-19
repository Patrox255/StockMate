package com.example.stockmate.data.dtos

import java.util.UUID

enum class MultiplierField {
    NAME,
    VALUE
}

data class MultiplierFormState (
    val localId: String = UUID.randomUUID().toString(),
    val name: String = "New Multiplier",
    val value: String = "1",
    val errors: Map<MultiplierField, List<String>> = emptyMap()
)