package com.example.stockmate.data.seeder

import com.example.stockmate.data.dao.DishDao
import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.ProductMultiplierDao
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Dish
import com.example.stockmate.data.entity.DishIngredient
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.entity.StockLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseSeeder @Inject constructor(
    val productMultiplierDao: ProductMultiplierDao,
    val stockLogDao: StockLogDao,
    val productDao: ProductDao,
    val dishDao: DishDao
) {
    suspend fun seedDatabase() {
        withContext(Dispatchers.IO) {
            val reservedProductIds = (1..10).map { it.toLong() }.toList()
            val reservedDishIds = listOf(1L, 2L, 3L)

            dishDao.deleteDishIngredientsByProductIds(reservedProductIds)
            dishDao.deleteDishesByIds(reservedDishIds)

            productMultiplierDao.deleteMultipliersByProductIds(reservedProductIds)
            stockLogDao.deleteLogsForProductIds(reservedProductIds)
            productDao.deleteProductsByIds(reservedProductIds)

            val idMilk = productDao.insertProduct(Product(id=1, name = "Milk", unit = "L", currentStock = 2.0f, targetStock = 5.0f))
            val idRice = productDao.insertProduct(Product(id=2, name = "Rice", unit = "kg", currentStock = 1.5f, targetStock = 4.0f))
            val idEggs = productDao.insertProduct(Product(id=3, name = "Eggs", unit = "pcs", currentStock = 8.0f, targetStock = 20.0f))
            val idBread = productDao.insertProduct(Product(id=4, name = "Bread", unit = "loaf", currentStock = 1.0f, targetStock = 2.0f))
            val idButter = productDao.insertProduct(Product(id=5, name = "Butter", unit = "g", currentStock = 150.0f, targetStock = 600.0f))
            val idChicken = productDao.insertProduct(Product(id=6, name = "Chicken Breast", unit = "kg", currentStock = 0.8f, targetStock = 2.0f))
            val idPasta = productDao.insertProduct(Product(id=7, name = "Pasta", unit = "g", currentStock = 400.0f, targetStock = 1500.0f))
            val idCoffee = productDao.insertProduct(Product(id=8, name = "Coffee", unit = "g", currentStock = 250.0f, targetStock = 1000.0f))
            val idPotatoes = productDao.insertProduct(Product(id=9, name = "Potatoes", unit = "kg", currentStock = 3.5f, targetStock = 10.0f))
            val idApples = productDao.insertProduct(Product(id=10, name = "Apples", unit = "kg", currentStock = 1.2f, targetStock = 3.0f))

            val sampleMultipliers = listOf<ProductMultiplier>(
                ProductMultiplier(productId = idMilk, name = "Glass", value = 0.25f, sortOrder = 1),
                ProductMultiplier(productId = idMilk, name = "Carton", value = 1.0f, sortOrder = 2),

                ProductMultiplier(productId = idRice, name = "Portion", value = 0.1f, sortOrder = 1),
                ProductMultiplier(productId = idRice, name = "Box", value = 1.0f, sortOrder = 2),

                ProductMultiplier(productId = idEggs, name = "Single", value = 1.0f, sortOrder = 1),
                ProductMultiplier(productId = idEggs, name = "Omelette", value = 3.0f, sortOrder = 2),

                ProductMultiplier(productId = idBread, name = "Slice", value = 0.05f, sortOrder = 1),
                ProductMultiplier(productId = idBread, name = "Half", value = 0.5f, sortOrder = 2),

                ProductMultiplier(productId = idButter, name = "Sandwich", value = 10.0f, sortOrder = 1),
                ProductMultiplier(productId = idButter, name = "Baking", value = 100.0f, sortOrder = 2),

                ProductMultiplier(productId = idChicken, name = "Dinner", value = 0.2f, sortOrder = 1),

                ProductMultiplier(productId = idPasta, name = "Bowl", value = 100.0f, sortOrder = 1),
                ProductMultiplier(productId = idPasta, name = "Pack", value = 500.0f, sortOrder = 2),

                ProductMultiplier(productId = idCoffee, name = "Mug", value = 15.0f, sortOrder = 1),

                ProductMultiplier(productId = idPotatoes, name = "Dinner", value = 0.5f, sortOrder = 1),

                ProductMultiplier(productId = idApples, name = "Single", value = 0.2f, sortOrder = 1)
            )

            val sampleMultipliersIds = sampleMultipliers.map { productMultiplierDao.insertMultiplier(it) }
            val idEggsSingleMultiplier = sampleMultipliersIds[sampleMultipliers.indexOfFirst {
                it.productId == idEggs && it.name == "Single"
            }]
            val idBreadMultiplierSingle = sampleMultipliersIds[sampleMultipliers.indexOfFirst {
                it.productId == idBread && it.name == "Slice"
            }]
            val idButterMultiplierSandwich = sampleMultipliersIds[sampleMultipliers.indexOfFirst {
                it.productId == idButter && it.name == "Sandwich"
            }]
            val idChickenDinnerMultiplier = sampleMultipliersIds[sampleMultipliers.indexOfFirst {
                it.productId == idChicken && it.name == "Dinner"
            }]
            val idPastaBowlMultiplier = sampleMultipliersIds[sampleMultipliers.indexOfFirst {
                it.productId == idPasta && it.name == "Bowl"
            }]
            val idMilkGlassMultiplier = sampleMultipliersIds[sampleMultipliers.indexOfFirst {
                it.productId == idMilk && it.name == "Glass"
            }]

            val dishEggsName = "Scrambled Eggs"
            val dishPastaName = "Chicken Pasta"
            val dishPancakesName = "Pancakes"
            val idDishEggs = dishDao.insertDish(Dish(
                id = 1,
                name = dishEggsName,
                description = "Classic morning breakfast with buttery toast.",
                createdAt = System.currentTimeMillis())
            )
            val idDishPasta = dishDao.insertDish(Dish(
                id = 2,
                name = dishPastaName,
                description = "Simple, high-protein delicious dinner.",
                createdAt = System.currentTimeMillis())
            )
            val idDishPancakes = dishDao.insertDish(Dish(
                id = 3,
                name = dishPancakesName,
                description = "Fluffy and delicious breakfast.",
                createdAt = System.currentTimeMillis())
            )

            val sampleDishIngredients = listOf(
                /// Scrambled Eggs: 3x Eggs (Single), 1x Butter (Sandwich portion), 2x Bread (Slice)
                DishIngredient(dishId = idDishEggs, productId = idEggs, amount = 3.0, multiplierId = idEggsSingleMultiplier),
                DishIngredient(dishId = idDishEggs, productId = idButter, amount = 1.0, multiplierId = idButterMultiplierSandwich),
                DishIngredient(dishId = idDishEggs, productId = idBread, amount = 2.0, multiplierId = idBreadMultiplierSingle),

                // Chicken Pasta: 1x Chicken (Dinner portion), 1x Pasta (Bowl)
                DishIngredient(dishId = idDishPasta, productId = idChicken, amount = 1.0, multiplierId = idChickenDinnerMultiplier),
                DishIngredient(dishId = idDishPasta, productId = idPasta, amount = 1.0, multiplierId = idPastaBowlMultiplier),

                // Pancakes: 2x Eggs (Single), 1x Butter (Sandwich portion), 1x Milk (Glass)
                DishIngredient(dishId = idDishPancakes, productId = idEggs, amount = 2.0, multiplierId = idEggsSingleMultiplier),
                DishIngredient(dishId = idDishPancakes, productId = idButter, amount = 1.0, multiplierId = idButterMultiplierSandwich),
                DishIngredient(dishId = idDishPancakes, productId = idMilk, amount = 1.0, multiplierId = idMilkGlassMultiplier)
            )
            sampleDishIngredients.forEach { dishDao.insertDishIngredient(it) }

            val sampleLogs = mutableListOf<StockLog>()
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            var curStock = 0.0f
            fun addStockLog(
                productId: Long,
                daysAgo: Int,
                amountChanged: Float,
                changeReason: ChangeReason,
                dishId: Long? = null,
                dishName: String? = null
            ) {
                curStock += amountChanged
                sampleLogs.add(
                    StockLog(
                        productId = productId,
                        timestamp = now - daysAgo * dayMs,
                        amountChanged = amountChanged,
                        changeReason = changeReason,
                        stockBefore = curStock - amountChanged,
                        relatedDishId = dishId,
                        relatedDishName = dishName
                    )
                )
            }

            // Simple milk scenario which was consumed over 7 days one glass per day
            addStockLog(
                productId = idMilk,
                daysAgo = 8,
                amountChanged = 2.0f,
                changeReason = ChangeReason.RESTOCKED
            )
            for (daysAgo in 7 downTo 1) {
                addStockLog(
                    productId = idMilk,
                    daysAgo = daysAgo,
                    amountChanged = -0.25f,
                    changeReason = ChangeReason.CONSUMED
                )
            }

//           // Scrambled eggs scenario where this dish was prepared every other day for a week, consuming 3 eggs, 1 butter portion, and 2 slices of bread each time
            addStockLog(
                productId = idEggs,
                daysAgo = 8,
                amountChanged = 30.0f,
                changeReason = ChangeReason.RESTOCKED
            )
            addStockLog(
                productId = idButter,
                daysAgo = 8,
                amountChanged = 1000.0f,
                changeReason = ChangeReason.RESTOCKED
            )
            addStockLog(
                productId = idBread,
                daysAgo = 8,
                amountChanged = 10.0f,
                changeReason = ChangeReason.RESTOCKED
            )
            for (daysAgo in 7 downTo 1 step 2) {
                addStockLog(
                    productId = idEggs,
                    daysAgo = daysAgo,
                    amountChanged = -3.0f,
                    changeReason = ChangeReason.CONSUMED,
                    dishId = idDishEggs,
                    dishName = dishEggsName
                )
                addStockLog(
                    productId = idButter,
                    daysAgo = daysAgo,
                    amountChanged = -10.0f,
                    changeReason = ChangeReason.CONSUMED,
                    dishId = idDishEggs,
                    dishName = dishEggsName
                )
                addStockLog(
                    productId = idBread,
                    daysAgo = daysAgo,
                    amountChanged = -0.1f,
                    changeReason = ChangeReason.CONSUMED,
                    dishId = idDishEggs,
                    dishName = dishEggsName
                )
            }

            listOf(3,1).forEach { daysAgo ->
                addStockLog(
                    productId = idEggs,
                    daysAgo = daysAgo,
                    amountChanged = -2.0f,
                    changeReason = ChangeReason.CONSUMED,
                    dishId = idDishPancakes,
                    dishName = dishPancakesName
                )
                addStockLog(
                    productId = idButter,
                    daysAgo = daysAgo,
                    amountChanged = -10.0f,
                    changeReason = ChangeReason.CONSUMED,
                    dishId = idDishPancakes,
                    dishName = dishPancakesName
                )
                addStockLog(
                    productId = idMilk,
                    daysAgo = daysAgo,
                    amountChanged = -0.25f,
                    changeReason = ChangeReason.CONSUMED,
                    dishId = idDishPancakes,
                    dishName = dishPancakesName
                )
            }

            // Coffee scenario with increasing consumption over 5 days
            addStockLog(
                productId = idCoffee,
                daysAgo = 6,
                amountChanged = 250f,
                changeReason = ChangeReason.RESTOCKED,
            )
            addStockLog(
                productId = idCoffee,
                daysAgo = 5,
                amountChanged = -15f,
                changeReason = ChangeReason.CONSUMED
            )
            addStockLog(
                productId = idCoffee,
                daysAgo = 4,
                amountChanged = -15f,
                changeReason = ChangeReason.CONSUMED
            )
            addStockLog(
                productId = idCoffee,
                daysAgo = 3,
                amountChanged = -30f,
                changeReason = ChangeReason.CONSUMED
            )
            addStockLog(
                productId = idCoffee,
                daysAgo = 2,
                amountChanged = -30f,
                changeReason = ChangeReason.CONSUMED
            )
            addStockLog(
                productId = idCoffee,
                daysAgo = 1,
                amountChanged = -45f,
                changeReason = ChangeReason.CONSUMED
            )

            sampleLogs.forEach { stockLogDao.insertLog(it) }
        }
    }
}
