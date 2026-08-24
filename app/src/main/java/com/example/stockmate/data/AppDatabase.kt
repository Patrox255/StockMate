package com.example.stockmate.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.stockmate.data.dao.DishDao
import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.ProductMultiplierDao
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.entity.Converters
import com.example.stockmate.data.entity.Dish
import com.example.stockmate.data.entity.DishIngredient
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductMultiplier
import com.example.stockmate.data.entity.StockLog

@Database(
    entities = [
        Product::class,
        StockLog::class,
        ProductMultiplier::class,
        Dish::class,
        DishIngredient::class
   ],
    version = 9,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun stockLogDao(): StockLogDao
    abstract fun productMultiplierDao(): ProductMultiplierDao
    abstract fun dishDao(): DishDao
}