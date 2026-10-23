package com.example.pipos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "risks",
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
data class RiskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val likelihood: Int = 3, // 1 to 5
    val impact: Int = 3, // 1 to 5
    val mitigationPlan: String = "",
    val ownerMemberId: String? = null,
    val status: String = "OPEN", // OPEN, MITIGATING, RESOLVED, ACCEPTED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Risk Exposure = Likelihood x Impact (Range: 1 - 25)
    val exposure: Int
        get() = likelihood * impact

    // Severity category based on documented thresholds
    val severityLevel: String
        get() = when {
            exposure >= 13 -> "HIGH"
            exposure >= 5 -> "MEDIUM"
            else -> "LOW"
        }
}
