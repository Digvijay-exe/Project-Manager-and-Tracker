package com.example.pipos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tasks",
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
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val status: String = "TODO", // TODO, IN_PROGRESS, BLOCKED, DONE
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val isReleaseBlocker: Boolean = false,
    val dueDate: Long? = null,
    val assignedMemberId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
