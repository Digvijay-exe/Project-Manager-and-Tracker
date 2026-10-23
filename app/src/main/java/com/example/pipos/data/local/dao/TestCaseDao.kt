package com.example.pipos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pipos.data.local.entity.TestCaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestCaseDao {
    @Query("SELECT * FROM test_cases WHERE projectId = :projectId ORDER BY name ASC")
    fun getTestCasesByProject(projectId: String): Flow<List<TestCaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestCase(testCase: TestCaseEntity)

    @Update
    suspend fun updateTestCase(testCase: TestCaseEntity)

    @Delete
    suspend fun deleteTestCase(testCase: TestCaseEntity)

    @Query("DELETE FROM test_cases WHERE id = :id")
    suspend fun deleteTestCaseById(id: String)

    @Query("DELETE FROM test_cases")
    suspend fun clearAll()
}
