package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index("categoryId", "month", "year", unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long, // 0 for Total Monthly Overall Budget, or specific categoryId
    val month: Int, // 1 - 12
    val year: Int,
    val amountPaise: Long,
    val warnThreshold75: Boolean = true,
    val warnThreshold90: Boolean = true,
    val warnThreshold100: Boolean = true
)
