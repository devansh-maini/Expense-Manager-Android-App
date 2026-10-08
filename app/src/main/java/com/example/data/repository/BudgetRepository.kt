package com.example.data.repository

import com.example.data.local.dao.BudgetDao
import com.example.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonth(month, year)
    }

    suspend fun getBudgetForCategory(categoryId: Long, month: Int, year: Int): BudgetEntity? {
        return budgetDao.getBudgetForCategory(categoryId, month, year)
    }

    fun getOverallBudgetForMonth(month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetDao.getOverallBudgetForMonth(month, year)
    }

    fun getAllBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getAllBudgets()
    }

    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }
}
