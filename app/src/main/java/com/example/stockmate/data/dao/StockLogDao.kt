package com.example.stockmate.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.stockmate.data.entity.StockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StockLogDao {
    @Query("SELECT * FROM stock_logs WHERE productId = :productId ORDER BY timestamp DESC")
    fun getLogsForProductFlow(productId: Long): Flow<List<StockLog>>

    @Query("SELECT * FROM stock_logs WHERE productId = :productId ORDER BY timestamp DESC")
    suspend fun getLogsForProduct(productId: Long): List<StockLog>

    @Insert
    suspend fun insertLog(log: StockLog)

    @Query("SELECT * FROM stock_logs WHERE changeReason = 'CONSUMED' ORDER BY timestamp DESC")
    fun getAllConsumptionsFlow(): Flow<List<StockLog>>

    @Query("DELETE FROM stock_logs WHERE productId IN (:productIds)")
    suspend fun deleteLogsForProductIds(productIds: List<Long>)

    @Query("SELECT * FROM stock_logs WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    suspend fun getLogsSince(timestamp: Long): List<StockLog>

    @Query("SELECT * FROM stock_logs WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getLogsSinceFlow(timestamp: Long): Flow<List<StockLog>>
}