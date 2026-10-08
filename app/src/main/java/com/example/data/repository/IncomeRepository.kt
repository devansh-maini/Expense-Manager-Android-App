package com.example.data.repository

import com.example.data.local.dao.IncomeDao
import com.example.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val incomeDao: IncomeDao) {
    val allIncome: Flow<List<IncomeEntity>> = incomeDao.getAllIncome()

    fun getIncomeInRange(startTime: Long, endTime: Long): Flow<List<IncomeEntity>> {
        return incomeDao.getIncomeInRange(startTime, endTime)
    }

    suspend fun getIncomeInRangeSync(startTime: Long, endTime: Long): List<IncomeEntity> {
        return incomeDao.getIncomeInRangeSync(startTime, endTime)
    }

    fun getTotalIncomeInRange(startTime: Long, endTime: Long): Flow<Long> {
        return incomeDao.getTotalIncomeInRange(startTime, endTime)
    }

    suspend fun getIncomeById(id: Long): IncomeEntity? {
        return incomeDao.getIncomeById(id)
    }

    suspend fun insertIncome(income: IncomeEntity): Long {
        return incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        incomeDao.deleteIncome(income)
    }

    suspend fun deleteIncomeById(id: Long) {
        incomeDao.deleteIncomeById(id)
    }
}
