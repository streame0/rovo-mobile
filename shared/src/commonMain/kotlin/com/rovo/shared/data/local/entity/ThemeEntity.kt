package com.rovo.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val textColor: Long,
    val textMutedColor: Long,
    val errorColor: Long,
    val isBuiltIn: Boolean = false,
    val category: String = "custom"
)
