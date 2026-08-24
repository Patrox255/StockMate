package com.example.stockmate.data.repository

import com.example.stockmate.data.dao.DishDao
import javax.inject.Inject

class DishRepository @Inject constructor(
    val dishDao: DishDao
) {
    suspend fun getDishWithIngredients(dishId: Long) = dishDao.getDishWithIngredients(dishId)
}
