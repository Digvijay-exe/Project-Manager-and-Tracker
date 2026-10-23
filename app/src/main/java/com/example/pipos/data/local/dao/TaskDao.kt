package com.example.pipos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pipos.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getTasksByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate < :currentTime AND status != 'DONE'")
    fun getOverdueTasks(currentTime: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}
