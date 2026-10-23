package com.example.pipos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pipos.data.local.entity.RiskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskDao {
    @Query("SELECT * FROM risks ORDER BY (likelihood * impact) DESC, createdAt DESC")
    fun getAllRisks(): Flow<List<RiskEntity>>

    @Query("SELECT * FROM risks WHERE projectId = :projectId ORDER BY (likelihood * impact) DESC")
    fun getRisksByProject(projectId: String): Flow<List<RiskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisk(risk: RiskEntity)

    @Update
    suspend fun updateRisk(risk: RiskEntity)

    @Delete
    suspend fun deleteRisk(risk: RiskEntity)

    @Query("DELETE FROM risks WHERE id = :id")
    suspend fun deleteRiskById(id: String)

    @Query("DELETE FROM risks")
    suspend fun clearAll()
}
