package com.example.pipos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pipos.data.local.entity.TeamMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members ORDER BY name ASC")
    fun getAllTeamMembers(): Flow<List<TeamMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(member: TeamMemberEntity)

    @Update
    suspend fun updateTeamMember(member: TeamMemberEntity)

    @Delete
    suspend fun deleteTeamMember(member: TeamMemberEntity)

    @Query("DELETE FROM team_members WHERE id = :id")
    suspend fun deleteTeamMemberById(id: String)

    @Query("DELETE FROM team_members")
    suspend fun clearAll()
}
