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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(
    private val db: AppDatabase,
    private val productDao: ProductDao,
    private val productMultiplierDao: ProductMultiplierDao,
    private val stockLogDao: StockLogDao
) {
    fun getAllProductsWithMultipliersFlow(): Flow<List<ProductWithMultipliers>> =
        productDao.getAllProductsWithMultipliersFlow()
            .map { products ->
                products.map { it.withSortedMultipliers() }
            }

    fun getProductWithMultipliersByIdFlow(id: Long): Flow<ProductWithMultipliers?> =
        productDao.getProductWithMultipliersFlow(id)
            .map { product ->
                product?.withSortedMultipliers()
            }

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
            productMultiplierDao.deleteMultipliersByProductIds(listOf(productId))
            productDao.deleteProductsByIds(listOf(productId))
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
            changeReason = reason
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
            // We start by updating the product simple data
            productDao.updateProduct(product)

            // Then we delete all existing multipliers for this product due to the fact that
            // when we load all the multipliers to the form data we set their respective id to
            // random values therefore we cannot update them directly, we have to delete the old ones and insert the new ones
            productMultiplierDao.deleteMultipliersByProductIds(listOf(product.id))

            // Finally, we insert the new multipliers
            multipliers.forEach { multiplier ->
                val multiplierWithProductId = multiplier.copy(productId = product.id)
                productMultiplierDao.insertMultiplier(multiplierWithProductId)
            }
        }
    }


    suspend fun seedSampleData() {
        val reservedIds = (1..10).map { it.toLong() }.toList()

        productMultiplierDao.deleteMultipliersByProductIds(reservedIds)
        productDao.deleteProductsByIds(reservedIds)

        val sampleProducts = listOf(
            Product(id = 1, name = "Milk", unit = "L", currentStock = 2.0f, targetStock = 5.0f, packageSize = 1.0f),
            Product(id = 2, name = "Rice", unit = "kg", currentStock = 1.5f, targetStock = 4.0f, packageSize = 1.0f),
            Product(id = 3, name = "Eggs", unit = "pcs", currentStock = 8.0f, targetStock = 20.0f, packageSize = 10.0f),
            Product(id = 4, name = "Bread", unit = "loaf", currentStock = 1.0f, targetStock = 2.0f, packageSize = 1.0f),
            Product(id = 5, name = "Butter", unit = "g", currentStock = 150.0f, targetStock = 600.0f, packageSize = 200.0f),
            Product(id = 6, name = "Chicken Breast", unit = "kg", currentStock = 0.8f, targetStock = 2.0f, packageSize = 0.5f),
            Product(id = 7, name = "Pasta", unit = "g", currentStock = 400.0f, targetStock = 1500.0f, packageSize = 500.0f),
            Product(id = 8, name = "Coffee", unit = "g", currentStock = 250.0f, targetStock = 1000.0f, packageSize = 500.0f),
            Product(id = 9, name = "Potatoes", unit = "kg", currentStock = 3.5f, targetStock = 10.0f, packageSize = 5.0f),
            Product(id = 10, name = "Apples", unit = "kg", currentStock = 1.2f, targetStock = 3.0f, packageSize = 1.0f)
        )

        sampleProducts.forEach { productDao.insertProduct(it) }

        val sampleMultipliers = listOf<ProductMultiplier>(
            ProductMultiplier(productId = 1, name = "Glass", value = 0.25f, sortOrder = 1),
            ProductMultiplier(productId = 1, name = "Carton", value = 1.0f, sortOrder = 2),

            ProductMultiplier(productId = 2, name = "Portion", value = 0.1f, sortOrder = 1),
            ProductMultiplier(productId = 2, name = "Box", value = 1.0f, sortOrder = 2),

            ProductMultiplier(productId = 3, name = "Single", value = 1.0f, sortOrder = 1),
            ProductMultiplier(productId = 3, name = "Omelette", value = 3.0f, sortOrder = 2),

            ProductMultiplier(productId = 4, name = "Slice", value = 0.05f, sortOrder = 1),
            ProductMultiplier(productId = 4, name = "Half", value = 0.5f, sortOrder = 2),

            ProductMultiplier(productId = 5, name = "Sandwich", value = 10.0f, sortOrder = 1),
            ProductMultiplier(productId = 5, name = "Baking", value = 100.0f, sortOrder = 2),

            ProductMultiplier(productId = 6, name = "Dinner", value = 0.2f, sortOrder = 1),

            ProductMultiplier(productId = 7, name = "Bowl", value = 100.0f, sortOrder = 1),
            ProductMultiplier(productId = 7, name = "Pack", value = 500.0f, sortOrder = 2),

            ProductMultiplier(productId = 8, name = "Mug", value = 15.0f, sortOrder = 1),

            ProductMultiplier(productId = 9, name = "Dinner", value = 0.5f, sortOrder = 1),

            ProductMultiplier(productId = 10, name = "Single", value = 0.2f, sortOrder = 1)
        )

        sampleMultipliers.forEach { productMultiplierDao.insertMultiplier(it) }
    }
}