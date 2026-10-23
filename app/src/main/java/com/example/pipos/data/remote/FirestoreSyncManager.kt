package com.example.pipos.data.remote

import android.content.Context
import com.example.R
import com.example.pipos.data.local.entity.MilestoneEntity
import com.example.pipos.data.local.entity.ProjectEntity
import com.example.pipos.data.local.entity.RiskEntity
import com.example.pipos.data.local.entity.TaskEntity
import com.example.pipos.data.local.entity.TeamMemberEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    private val auth = Firebase.auth

    val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserName: String?
        get() = auth.currentUser?.displayName ?: auth.currentUser?.email

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google to perform cloud operations.")
    }

    suspend fun syncProjectToCloud(project: ProjectEntity): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("projects").document(project.id)
            val data = hashMapOf(
                "id" to project.id,
                "name" to project.name,
                "description" to project.description,
                "status" to project.status,
                "startDate" to project.startDate,
                "deadlineDate" to project.deadlineDate,
                "budget" to project.budget,
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/projects/${project.id}")
            Result.failure(e)
        }
    }

    suspend fun syncTaskToCloud(task: TaskEntity): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("tasks").document(task.id)
            val data = hashMapOf(
                "id" to task.id,
                "projectId" to task.projectId,
                "title" to task.title,
                "description" to task.description,
                "status" to task.status,
                "priority" to task.priority,
                "isReleaseBlocker" to task.isReleaseBlocker,
                "dueDate" to task.dueDate,
                "assignedMemberId" to task.assignedMemberId,
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/tasks/${task.id}")
            Result.failure(e)
        }
    }

    suspend fun syncRiskToCloud(risk: RiskEntity): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("risks").document(risk.id)
            val data = hashMapOf(
                "id" to risk.id,
                "projectId" to risk.projectId,
                "title" to risk.title,
                "description" to risk.description,
                "likelihood" to risk.likelihood,
                "impact" to risk.impact,
                "mitigationPlan" to risk.mitigationPlan,
                "ownerMemberId" to risk.ownerMemberId,
                "status" to risk.status,
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/risks/${risk.id}")
            Result.failure(e)
        }
    }

    suspend fun syncMilestoneToCloud(milestone: MilestoneEntity): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("milestones").document(milestone.id)
            val data = hashMapOf(
                "id" to milestone.id,
                "projectId" to milestone.projectId,
                "title" to milestone.title,
                "description" to milestone.description,
                "targetDate" to milestone.targetDate,
                "status" to milestone.status,
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/milestones/${milestone.id}")
            Result.failure(e)
        }
    }

    suspend fun syncTeamMemberToCloud(member: TeamMemberEntity): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("team_members").document(member.id)
            val data = hashMapOf(
                "id" to member.id,
                "name" to member.name,
                "role" to member.role,
                "email" to member.email,
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/team_members/${member.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteProjectFromCloud(projectId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).collection("projects").document(projectId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/${auth.currentUser?.uid}/projects/$projectId")
            Result.failure(e)
        }
    }

    suspend fun fetchCloudProjects(): Result<List<ProjectEntity>> {
        return try {
            val uid = requireUserId()
            val snapshot = db.collection("users").document(uid).collection("projects").get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                val desc = doc.getString("description") ?: ""
                val status = doc.getString("status") ?: "ACTIVE"
                val startDate = doc.getLong("startDate") ?: System.currentTimeMillis()
                val deadlineDate = doc.getLong("deadlineDate")
                val budget = doc.getDouble("budget")
                ProjectEntity(
                    id = id,
                    name = name,
                    description = desc,
                    status = status,
                    startDate = startDate,
                    deadlineDate = deadlineDate,
                    budget = budget
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "users/${auth.currentUser?.uid}/projects")
            Result.failure(e)
        }
    }
}
