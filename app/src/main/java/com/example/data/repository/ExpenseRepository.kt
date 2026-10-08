package com.example.data.repository

import com.example.data.local.dao.ExpenseDao
import com.example.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesInRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesInRange(startTime, endTime)
    }

    suspend fun getExpensesInRangeSync(startTime: Long, endTime: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesInRangeSync(startTime, endTime)
    }

    fun getTotalExpensesInRange(startTime: Long, endTime: Long): Flow<Long> {
        return expenseDao.getTotalExpensesInRange(startTime, endTime)
    }

    fun getTotalSpentForCategoryInRange(categoryId: Long, startTime: Long, endTime: Long): Flow<Long> {
        return expenseDao.getTotalSpentForCategoryInRange(categoryId, startTime, endTime)
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
