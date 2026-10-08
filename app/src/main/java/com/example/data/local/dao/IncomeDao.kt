package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("SELECT * FROM income ORDER BY timestamp DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getIncomeInRange(startTime: Long, endTime: Long): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getIncomeInRangeSync(startTime: Long, endTime: Long): List<IncomeEntity>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM income WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalIncomeInRange(startTime: Long, endTime: Long): Flow<Long>
}
