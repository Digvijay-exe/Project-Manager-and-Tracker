package com.example.pipos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class MilestoneEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val targetDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)
