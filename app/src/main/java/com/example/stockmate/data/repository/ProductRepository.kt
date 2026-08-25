package com.example.stockmate.data.repository

import androidx.room3.withWriteTransaction
import com.example.stockmate.data.AppDatabase
import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.ProductMultiplierDao
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.entity.StockLog
import com.example.stockmate.data.util.img.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val db: AppDatabase,
    private val productDao: ProductDao,
    private val productMultiplierDao: ProductMultiplierDao,
    private val stockLogDao: StockLogDao,
    private val imageStorage: ImageStorage
) {
    fun getAllProductsWithMultipliersFlow(): Flow<List<ProductWithMultipliers>> =
        productDao.getAllProductsWithMultipliersFlow()
            .map { products ->
                products.map { it.withSortedMultipliers() }
            }

    fun getMultipliersByProductIdFlow(productId: Long): Flow<List<ProductMultiplier>> =
        productMultiplierDao.getMultipliersByProductIdFlow(productId)
            .map { multipliers ->
                multipliers.sortedBy { it.sortOrder }
            }

    fun getAllProductsFlow(): Flow<List<Product>> =
        productDao.getAllProductsFlow()

    fun getProductWithMultipliersByIdFlow(id: Long): Flow<ProductWithMultipliers?> =
        productDao.getProductWithMultipliersFlow(id)
            .map { product ->
                product?.withSortedMultipliers()
            }

    suspend fun getProductsByIds(ids: List<Long>): List<Product> =
        productDao.getProductsByIds(ids)

    suspend fun getProductWithMultipliersById(id: Long): ProductWithMultipliers? =
        productDao.getProductWithMultipliers(id)?.withSortedMultipliers()

    private fun ProductWithMultipliers.withSortedMultipliers(): ProductWithMultipliers {
        return this.copy(
            multipliers = this.multipliers.sortedBy { it.sortOrder }
        )
    }

    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun insertProductMultiplier(multiplier: ProductMultiplier): Long {
        return productMultiplierDao.insertMultiplier(multiplier)
    }

    suspend fun deleteProduct(productId: Long) {
        db.withWriteTransaction {
            val productToDelete = productDao.getProductById(productId)
            productMultiplierDao.deleteMultipliersByProductIds(listOf(productId))
            productDao.deleteProductsByIds(listOf(productId))
            productToDelete?.imageUrl?.let { imagePath ->
                imageStorage.deleteImage(imagePath)
            }
        }
    }

    suspend fun changeStock(product: Product, amount: Float, reason: ChangeReason) {
        val newStock = product.currentStock + amount
        val updatedProduct = product.copy(currentStock = newStock)
        productDao.updateProduct(updatedProduct)

        val log = StockLog(
            productId = product.id,
            timestamp = System.currentTimeMillis(),
            amountChanged = amount,
            changeReason = reason,
            stockBefore = product.currentStock
        )
        stockLogDao.insertLog(log)
    }

    suspend fun insertProductWithMultipliers(product: Product, multipliers: List<ProductMultiplier>) {
        db.withWriteTransaction {
            val productId = productDao.insertProduct(product)

            multipliers.forEach { multiplier ->
                val multiplierWithProductId = multiplier.copy(productId = productId)
                productMultiplierDao.insertMultiplier(multiplierWithProductId)
            }
        }
    }

    suspend fun updateProductWithMultipliers(
        product: Product,
        multipliers: List<ProductMultiplier>
    ) {
        db.withWriteTransaction {
            // At first we update the product plain data
            productDao.updateProduct(product)

            // Then we check which multipliers are completely new
            val existingMultipliers = productMultiplierDao.getMultipliersByProductId(product.id)
            val existingIds = existingMultipliers.map { it.id }.toSet()
            val newIds = multipliers
                .map {it.id}
                .filter {it != 0L}
                .toSet()

            // We delete the multipliers that are no longer present in the new list
            val idsToDelete = existingIds - newIds
            if (idsToDelete.isNotEmpty()) {
                productMultiplierDao.deleteMultipliersByIds(idsToDelete.toList())
            }

            // Finally we insert or update the multipliers that are present in the new list
            multipliers.forEach { multiplier ->
                if (multiplier.id == 0L) {
                    productMultiplierDao.insertMultiplier(multiplier.copy(productId = product.id))
                }
                else {
                    productMultiplierDao.updateMultiplier(multiplier.copy(productId = product.id))
                }
            }

        }
    }
}