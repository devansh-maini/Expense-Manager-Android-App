package com.example.data.repository

import com.example.data.local.dao.BillDao
import com.example.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val upcomingUnpaidBills: Flow<List<BillEntity>> = billDao.getUpcomingUnpaidBills()

    fun getUpcomingBillsLimit(limit: Int): Flow<List<BillEntity>> {
        return billDao.getUpcomingBillsLimit(limit)
    }

    suspend fun getBillById(id: Long): BillEntity? {
        return billDao.getBillById(id)
    }

    suspend fun insertBill(bill: BillEntity): Long {
        return billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    suspend fun setPaidStatus(id: Long, isPaid: Boolean) {
        billDao.updatePaidStatus(id, isPaid)
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun deleteBillById(id: Long) {
        billDao.deleteBillById(id)
    }
}
