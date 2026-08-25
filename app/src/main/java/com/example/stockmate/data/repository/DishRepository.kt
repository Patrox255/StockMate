package com.example.stockmate.data.repository

import androidx.room3.withWriteTransaction
import com.example.stockmate.data.AppDatabase
import com.example.stockmate.data.dao.DishDao
import com.example.stockmate.data.entity.Dish
import com.example.stockmate.data.entity.DishIngredient
import javax.inject.Inject

class DishRepository @Inject constructor(
    val dishDao: DishDao,
    val appDatabase: AppDatabase
) {
    suspend fun getDishWithIngredients(dishId: Long) = dishDao.getDishWithIngredients(dishId)
    suspend fun updateDish(dish: Dish) = dishDao.updateDish(dish)
    suspend fun insertDish(dish: Dish) = dishDao.insertDish(dish)
    suspend fun updateDishWithIngredients(dish: Dish, ingredients: List<DishIngredient>) {
        appDatabase.withWriteTransaction {
            dishDao.updateDish(dish)

            val existingIngredients = dishDao.getDishIngredients(dish.id)
            val existingIngredientsIds = existingIngredients.map {it.id}.toSet()
            val newIngredientsIds = ingredients.map {it.id}.toSet()
            val ingredientsToDeleteIds = existingIngredientsIds - newIngredientsIds

            if (ingredientsToDeleteIds.isNotEmpty()) {
                ingredientsToDeleteIds.forEach { id ->
                    dishDao.deleteDishIngredient(id)
                }
            }

            ingredients.forEach { ingredient ->
                if (ingredient.id == 0L) {
                    dishDao.insertDishIngredient(ingredient)
                } else {
                    dishDao.updateDishIngredient(ingredient)
                }
            }
        }
    }
    suspend fun insertDishWithIngredients(dish: Dish, ingredients: List<DishIngredient>) {
        appDatabase.withWriteTransaction {
            val dishId = dishDao.insertDish(dish)
            ingredients.forEach { ingredient ->
                val ingredientWithDishId = ingredient.copy(dishId = dishId)
                dishDao.insertDishIngredient(ingredientWithDishId)
            }
        }
    }
    fun getDishesWithIngredientsFlow() = dishDao.getDishesWithIngredientsFlow()
    fun getDishWithIngredientsFlow(dishId: Long) = dishDao.getDishWithIngredientsFlow(dishId)
}
