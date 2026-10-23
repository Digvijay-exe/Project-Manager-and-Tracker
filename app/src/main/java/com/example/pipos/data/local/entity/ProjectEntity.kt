package com.example.pipos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val status: String = "ACTIVE", // ACTIVE, IN_PROGRESS, ON_HOLD, COMPLETED, ARCHIVED
    val startDate: Long = System.currentTimeMillis(),
    val deadlineDate: Long? = null,
    val budget: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
