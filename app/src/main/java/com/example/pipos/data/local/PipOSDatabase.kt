package com.example.pipos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pipos.data.local.dao.MilestoneDao
import com.example.pipos.data.local.dao.ProjectDao
import com.example.pipos.data.local.dao.RiskDao
import com.example.pipos.data.local.dao.TaskDao
import com.example.pipos.data.local.dao.TeamMemberDao
import com.example.pipos.data.local.dao.TestCaseDao
import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.example.pipos.data.local.entity.TestCaseEntity

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        RiskEntity::class,
        MilestoneEntity::class,
        TeamMemberEntity::class,
        TestCaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PipOSDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun riskDao(): RiskDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun testCaseDao(): TestCaseDao

    companion object {
        @Volatile
        private var INSTANCE: PipOSDatabase? = null

        fun getDatabase(context: Context): PipOSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PipOSDatabase::class.java,
                    "pip_os_protocol.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
