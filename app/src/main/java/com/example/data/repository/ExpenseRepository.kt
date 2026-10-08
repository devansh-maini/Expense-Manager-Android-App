package com.example.data.repository

import com.example.data.local.dao.ExpenseDao
import com.example.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun getAllExpensesSync(): List<ExpenseEntity> {
        return expenseDao.getAllExpensesSync()
    }

    suspend fun searchExpensesSync(query: String): List<ExpenseEntity> {
        return expenseDao.searchExpensesSync(query)
    }

    fun getExpensesInRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesInRange(startTime, endTime)
    }

    suspend fun getExpensesInRangeSync(startTime: Long, endTime: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesInRangeSync(startTime, endTime)
    }

    suspend fun getExpensesByCategoryAndRangeSync(categoryId: Long, startTime: Long, endTime: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesByCategoryAndRangeSync(categoryId, startTime, endTime)
    }

    suspend fun getExpensesByCategorySync(categoryId: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesByCategorySync(categoryId)
    }

    suspend fun getLargestExpenseInRangeSync(startTime: Long, endTime: Long): ExpenseEntity? {
        return expenseDao.getLargestExpenseInRangeSync(startTime, endTime)
    }

    suspend fun getLargestExpenseSync(): ExpenseEntity? {
        return expenseDao.getLargestExpenseSync()
    }

    fun getTotalExpensesInRange(startTime: Long, endTime: Long): Flow<Long> {
        return expenseDao.getTotalExpensesInRange(startTime, endTime)
    }

    suspend fun getTotalExpensesInRangeSync(startTime: Long, endTime: Long): Long {
        return expenseDao.getTotalExpensesInRangeSync(startTime, endTime)
    }

    fun getTotalSpentForCategoryInRange(categoryId: Long, startTime: Long, endTime: Long): Flow<Long> {
        return expenseDao.getTotalSpentForCategoryInRange(categoryId, startTime, endTime)
    }

    suspend fun getTotalSpentForCategoryInRangeSync(categoryId: Long, startTime: Long, endTime: Long): Long {
        return expenseDao.getTotalSpentForCategoryInRangeSync(categoryId, startTime, endTime)
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? {
        return expenseDao.getExpenseById(id)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    fun getRecurringExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getRecurringExpenses()
    }
}
