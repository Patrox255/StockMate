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

class ProductRepository(
    private val db: AppDatabase,
    private val productDao: ProductDao,
    private val productMultiplierDao: ProductMultiplierDao,
    private val stockLogDao: StockLogDao
) {
    fun getAllProductsWithMultipliers(): Flow<List<ProductWithMultipliers>> = productDao.getAllProductsWithMultipliersFlow()

    fun getProductById(id: Int): Flow<ProductWithMultipliers> = productDao.getProductWithMultipliersFlow(id)

    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun insertProductMultiplier(multiplier: ProductMultiplier): Long {
        return productMultiplierDao.insertMultiplier(multiplier)
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

    suspend fun seedSampleData() {
        val reservedIds = (1..10).toList()

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
            ProductMultiplier(productId = 1, name = "Glass", value = 0.25f),
            ProductMultiplier(productId = 1, name = "Carton", value = 1.0f),

            ProductMultiplier(productId = 2, name = "Portion", value = 0.1f),
            ProductMultiplier(productId = 2, name = "Box", value = 1.0f),

            ProductMultiplier(productId = 3, name = "Single", value = 1.0f),
            ProductMultiplier(productId = 3, name = "Omelette", value = 3.0f),

            ProductMultiplier(productId = 4, name = "Slice", value = 0.05f),
            ProductMultiplier(productId = 4, name = "Half", value = 0.5f),

            ProductMultiplier(productId = 5, name = "Sandwich", value = 10.0f),
            ProductMultiplier(productId = 5, name = "Baking", value = 100.0f),

            ProductMultiplier(productId = 6, name = "Dinner", value = 0.2f),

            ProductMultiplier(productId = 7, name = "Bowl", value = 100.0f),
            ProductMultiplier(productId = 7, name = "Pack", value = 500.0f),

            ProductMultiplier(productId = 8, name = "Mug", value = 15.0f),

            ProductMultiplier(productId = 9, name = "Dinner", value = 0.5f),

            ProductMultiplier(productId = 10, name = "Single", value = 0.2f)
        )

        sampleMultipliers.forEach { productMultiplierDao.insertMultiplier(it) }
    }
}