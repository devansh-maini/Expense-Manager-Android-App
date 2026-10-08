package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    indices = [
        Index("dueDate"),
        Index("categoryId")
    ]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountPaise: Long,
    val dueDate: Long, // Epoch millis
    val categoryId: Long,
    val isPaid: Boolean = false,
    val recurrence: String = "MONTHLY", // "NONE", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"
    val reminderDaysBefore: Int = 3,
    val notes: String = ""
)
