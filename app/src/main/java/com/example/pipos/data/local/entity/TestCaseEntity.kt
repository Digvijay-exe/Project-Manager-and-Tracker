package com.example.pipos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "test_cases",
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
data class TestCaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val name: String,
    val component: String = "CORE",
    val status: String = "UNTESTED", // UNTESTED, PASSED, FAILED
    val notes: String = "",
    val testedAt: Long? = null
)
