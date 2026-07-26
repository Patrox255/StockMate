package com.example.stockmate.data.prediction.math

data class Vector(
    val values: DoubleArray
) {
    val size: Int
        get() = values.size

    operator fun get(index: Int): Double {
        return values[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector

        if (!values.contentEquals(other.values)) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + size
        return result
    }
}

class DataFrame(
    private val columns: Map<String, Vector>
) {
    fun column(name: String): Vector {
        return columns[name]
            ?: error("Column $name does not exist in the DataFrame")
    }

    val rowCount: Int
        get() = columns.values.firstOrNull()?.size ?: 0
}