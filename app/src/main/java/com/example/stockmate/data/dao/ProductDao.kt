package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.entity.ProductWithMultipliers
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>

    // --- Whenever we want to display product's multipliers then we have to sort them by their sort order property
    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductWithMultipliersFlow(productId: Long): Flow<ProductWithMultipliers?>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductWithMultipliers(productId: Long): ProductWithMultipliers?

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Long): Product?

    @Transaction
    @Query("SELECT * FROM products")
    fun getAllProductsWithMultipliersFlow(): Flow<List<ProductWithMultipliers>>
    // ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id in (:ids)")
    suspend fun deleteProductsByIds(ids: List<Long>)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("SELECT * FROM products WHERE currentStock < targetStock")
    fun getShoppingListFlow(): Flow<List<Product>>
}