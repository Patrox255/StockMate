package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.stockmate.data.entity.ProductMultiplier

@Dao
interface ProductMultiplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiplier(multiplier: ProductMultiplier): Long

    @Query("DELETE FROM product_multipliers")
    suspend fun deleteAllMultipliers()

    @Query("DELETE FROM product_multipliers WHERE productId IN (:productIds)")
    suspend fun deleteMultipliersByProductIds(productIds: List<Int>)
}