package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.stockmate.data.entity.ProductMultiplier

@Dao
interface ProductMultiplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiplier(multiplier: ProductMultiplier): Long

    @Query("DELETE FROM product_multipliers")
    suspend fun deleteAllMultipliers()

    @Query("SELECT * FROM product_multipliers WHERE productId = :productId ORDER BY sortOrder ASC")
    suspend fun getMultipliersByProductId(productId: Long): List<ProductMultiplier>

    @Query("DELETE FROM product_multipliers WHERE productId IN (:productIds)")
    suspend fun deleteMultipliersByProductIds(productIds: List<Long>)

    @Query("DELETE FROM product_multipliers WHERE id IN (:multiplierIds)")
    suspend fun deleteMultipliersByIds(multiplierIds: List<Long>)

    @Update
    suspend fun updateMultiplier(productMultiplier: ProductMultiplier)
}