package com.example.pipos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String = "Engineer",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
