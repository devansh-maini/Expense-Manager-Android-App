package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("categoryId"),
        Index("paymentMethodId"),
        Index("timestamp")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long, // Integer minor unit: 100 paise = ₹1.00
    val categoryId: Long,
    val subCategory: String? = null,
    val timestamp: Long, // Epoch millis
    val paymentMethodId: Long,
    val merchant: String = "",
    val description: String = "",
    val receiptUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
)
