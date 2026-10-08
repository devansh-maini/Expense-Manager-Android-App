package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "Category", // Material icon key identifier
    val colorHex: Long = 0xFF2E7D32, // Primary color for charts and chips
    val subCategories: List<String> = emptyList(),
    val isDefault: Boolean = false
)
