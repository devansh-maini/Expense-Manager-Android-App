package com.example.data.repository

import com.example.data.local.dao.PaymentMethodDao
import com.example.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

class PaymentMethodRepository(private val paymentMethodDao: PaymentMethodDao) {
    val allPaymentMethods: Flow<List<PaymentMethodEntity>> = paymentMethodDao.getAllPaymentMethods()

    suspend fun getAllPaymentMethodsSync(): List<PaymentMethodEntity> {
        return paymentMethodDao.getAllPaymentMethodsSync()
    }

    suspend fun getPaymentMethodById(id: Long): PaymentMethodEntity? {
        return paymentMethodDao.getPaymentMethodById(id)
    }

    suspend fun insertPaymentMethod(method: PaymentMethodEntity): Long {
        return paymentMethodDao.insertPaymentMethod(method)
    }

    suspend fun updatePaymentMethod(method: PaymentMethodEntity) {
        paymentMethodDao.updatePaymentMethod(method)
    }

    suspend fun deletePaymentMethod(method: PaymentMethodEntity) {
        paymentMethodDao.deletePaymentMethod(method)
    }

    suspend fun deletePaymentMethodById(id: Long) {
        paymentMethodDao.deletePaymentMethodById(id)
    }
}
