package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.example.stockmate.data.entity.DishWithIngredients

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes WHERE id = :dishId")
    suspend fun getDishWithIngredients(dishId: Long): DishWithIngredients?
}