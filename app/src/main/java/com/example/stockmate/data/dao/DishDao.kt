package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy.Companion.REPLACE
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.example.stockmate.data.entity.Dish
import com.example.stockmate.data.entity.DishIngredient
import com.example.stockmate.data.entity.DishWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Transaction
    @Query("SELECT * FROM dishes WHERE id = :dishId")
    suspend fun getDishWithIngredients(dishId: Long): DishWithIngredients?

    @Transaction
    @Query("SELECT * FROM dishes WHERE id = :dishId")
    fun getDishWithIngredientsFlow(dishId: Long): Flow<DishWithIngredients?>

    @Transaction
    @Query("SELECT * FROM dishes")
    fun getDishesWithIngredientsFlow(): Flow<List<DishWithIngredients>>

    @Update
    suspend fun updateDish(dish: Dish)

    @Update
    suspend fun updateDishIngredient(ingredient: DishIngredient)

    @Query("DELETE FROM dishes")
    suspend fun deleteAllDishes()

    @Insert(onConflict = REPLACE)
    suspend fun insertDish(dish: Dish): Long

    @Insert
    suspend fun insertDishIngredient(ingredient: DishIngredient): Long

    @Query("SELECT * FROM dish_ingredients WHERE dishId = :dishId")
    suspend fun getDishIngredients(dishId: Long): List<DishIngredient>

    @Query("DELETE FROM dish_ingredients WHERE id = :ingredientId")
    suspend fun deleteDishIngredient(ingredientId: Long)

    @Query("DELETE FROM dish_ingredients WHERE productId IN (:productIds)")
    suspend fun deleteDishIngredientsByProductIds(productIds: List<Long>)

    @Query("DELETE FROM dishes WHERE id IN (:dishIds)")
    suspend fun deleteDishesByIds(dishIds: List<Long>)
    @Query("DELETE FROM dish_ingredients")
    suspend fun deleteAllDishIngredients()
}