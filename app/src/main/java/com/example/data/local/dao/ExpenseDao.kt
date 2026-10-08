package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' OR merchant LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchExpensesSync(query: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getExpensesInRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getExpensesInRangeSync(startTime: Long, endTime: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getExpensesByCategoryAndRangeSync(categoryId: Long, startTime: Long, endTime: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY amountPaise DESC LIMIT 1")
    suspend fun getLargestExpenseInRangeSync(startTime: Long, endTime: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY amountPaise DESC LIMIT 1")
    suspend fun getLargestExpenseSync(): ExpenseEntity?

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalExpensesInRange(startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalExpensesInRangeSync(startTime: Long, endTime: Long): Long

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE categoryId = :categoryId AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalSpentForCategoryInRange(categoryId: Long, startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE categoryId = :categoryId AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalSpentForCategoryInRangeSync(categoryId: Long, startTime: Long, endTime: Long): Long

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    suspend fun getExpensesByCategorySync(categoryId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE isRecurring = 1 ORDER BY timestamp DESC")
    fun getRecurringExpenses(): Flow<List<ExpenseEntity>>
}
