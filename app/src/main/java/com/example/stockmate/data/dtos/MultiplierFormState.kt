package com.example.stockmate.data.dtos

import java.util.UUID

data class MultiplierFormState (
    // Only for form validation purposes
    val localId: String = UUID.randomUUID().toString(),

    val databaseId: Long? = null,
    val name: String = "New Multiplier",
    val value: String = "1",
    val sortOrder: Int = 0
)