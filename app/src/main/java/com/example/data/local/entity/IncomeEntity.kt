package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income",
    indices = [
        Index("timestamp")
    ]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long, // Integer minor units (paise)
    val source: String, // e.g. "Monthly Salary", "Freelancing", "Investment", etc.
    val timestamp: Long,
    val notes: String = "",
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null
)
