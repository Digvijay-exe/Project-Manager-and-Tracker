package com.example.pipos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pipos.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY targetDate ASC")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE projectId = :projectId ORDER BY targetDate ASC")
    fun getMilestonesByProject(projectId: String): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: String)

    @Query("DELETE FROM milestones")
    suspend fun clearAll()
}
