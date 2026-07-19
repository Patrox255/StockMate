package com.example.stockmate.data.entity

import androidx.room3.ColumnTypeConverter

class Converters {
    @ColumnTypeConverter
    fun fromChangeReason(value: ChangeReason): String {
        return value.name
    }

    @ColumnTypeConverter
    fun toChangeReason(value: String): ChangeReason {
        return ChangeReason.valueOf(value)
    }
}