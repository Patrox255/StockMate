package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.StockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StockLogDao {
    @Query("SELECT * FROM stock_logs WHERE productId = :productId ORDER BY timestamp DESC")
    fun getLogsForProduct(productId: Int): Flow<List<StockLog>>

    @Insert
    suspend fun insertLog(log: StockLog)

    @Query("SELECT * FROM stock_logs WHERE changeReason = 'CONSUMED' ORDER BY timestamp DESC")
    fun getAllConsumptionsFlow(): Flow<List<StockLog>>
}