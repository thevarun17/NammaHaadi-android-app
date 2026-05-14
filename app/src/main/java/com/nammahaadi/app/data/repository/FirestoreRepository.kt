package com.nammahaadi.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nammahaadi.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirestoreRepository — real-time Firestore database access.
 *
 * Collections used:
 *   /reports         — road issue reports
 *   /report_updates  — status updates on reports
 *   /alerts          — community road alerts
 *   /users           — user profiles & points
 *
 * Flows below use Firestore snapshot listeners so the UI updates live
 * whenever data changes in the database — no polling needed.
 */
@Singleton
class FirestoreRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val storage = Firebase.storage.reference

    // ─── Storage ────────────────────────────────────────────────────────────

    suspend fun uploadImage(uri: android.net.Uri, folder: String): Result<String> =
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val fileRef = storage.child("$folder/$fileName")
            
            // Standard Firebase upload pattern
            val uploadTask = fileRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("does not exist")) {
                Result.Error("Upload failed: Firebase Storage Rules might be blocking the request. Check your console.")
            } else {
                Result.Error(msg.ifBlank { "Failed to upload image" })
            }
        }

    // ─── Reports ────────────────────────────────────────────────────────────

    /**
     * Real-time stream of all reports.
     * Optionally filtered by type and/or status.
     * The Flow emits a new list every time the Firestore collection changes.
     */
    fun observeReports(
        type: String? = null,
        status: String? = null
    ): Flow<List<RoadReport>> = callbackFlow {
        var query: Query = db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
        if (type != null) query = query.whereEqualTo("type", type)
        if (status != null) query = query.whereEqualTo("status", status)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val reports = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RoadReport::class.java)?.copy(id = doc.id)
            }
            trySend(reports)
        }
        awaitClose { listener.remove() }
    }

    /** Single fetch (non-realtime) — useful for one-off reads. */
    suspend fun getReports(type: String? = null, status: String? = null): Result<List<RoadReport>> =
        try {
            var query: Query = db.collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
            if (type != null) query = query.whereEqualTo("type", type)
            if (status != null) query = query.whereEqualTo("status", status)

            val snapshot = query.get().await()
            val reports = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RoadReport::class.java)?.copy(id = doc.id)
            }
            Result.Success(reports)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Firestore error")
        }

    suspend fun createReport(request: CreateReportRequest): Result<RoadReport> =
        try {
            val data = hashMapOf(
                "type" to request.type,
                "severity" to request.severity,
                "title" to request.title,
                "address" to request.address,
                "description" to request.description,
                "lat" to request.lat,
                "lng" to request.lng,
                "status" to "ACTIVE",
                "reportedBy" to request.reportedBy,
                "reportedByName" to request.reportedByName,
                "photoUrl" to request.photoUrl,
                "reportCount" to 1,
                "createdAt" to com.google.firebase.Timestamp.now().toDate().toString(),
                "updatedAt" to com.google.firebase.Timestamp.now().toDate().toString()
            )
            val docRef = db.collection("reports").add(data).await()
            // Increment user points for new report
            incrementUserPoints(request.reportedBy, 10, "reportsCount")
            val created = RoadReport(
                id = docRef.id,
                type = request.type,
                severity = request.severity,
                title = request.title,
                address = request.address,
                description = request.description,
                lat = request.lat,
                lng = request.lng,
                status = "ACTIVE",
                reportedBy = request.reportedBy,
                reportedByName = request.reportedByName,
                photoUrl = request.photoUrl
            )
            Result.Success(created)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create report")
        }

    suspend fun updateReportStatus(reportId: String, newStatus: String): Result<Unit> =
        try {
            db.collection("reports").document(reportId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to com.google.firebase.Timestamp.now().toDate().toString()
                    )
                ).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update status")
        }

    // ─── Report Updates ──────────────────────────────────────────────────────

    suspend fun createReportUpdate(request: CreateUpdateRequest): Result<ReportUpdate> =
        try {
            val data = hashMapOf(
                "reportId" to request.reportId,
                "reportedBy" to request.reportedBy,
                "reportedByName" to request.reportedByName,
                "newStatus" to request.newStatus,
                "note" to request.note,
                "lat" to request.lat,
                "lng" to request.lng,
                "photoUrl" to request.photoUrl,
                "createdAt" to com.google.firebase.Timestamp.now().toDate().toString()
            )
            val docRef = db.collection("report_updates").add(data).await()
            // Increment user points for update
            incrementUserPoints(request.reportedBy, 5, "updatesCount")
            // Also update the report's status field
            updateReportStatus(request.reportId, request.newStatus)
            Result.Success(
                ReportUpdate(
                    id = docRef.id,
                    reportId = request.reportId,
                    reportedBy = request.reportedBy,
                    reportedByName = request.reportedByName,
                    newStatus = request.newStatus,
                    note = request.note
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to submit update")
        }

    // ─── Alerts ─────────────────────────────────────────────────────────────

    /**
     * Real-time stream of alerts.
     * New alerts created by anyone appear instantly on all devices.
     */
    fun observeAlerts(): Flow<List<Alert>> = callbackFlow {
        val listener = db.collection("alerts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val alerts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Alert::class.java)?.copy(id = doc.id)
                }
                trySend(alerts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createAlert(request: CreateAlertRequest): Result<Alert> =
        try {
            val data = hashMapOf(
                "title" to request.title,
                "message" to request.message,
                "severity" to request.severity,
                "area" to request.area,
                "reportedBy" to request.reportedBy,
                "reportedByName" to request.reportedByName,
                "createdAt" to com.google.firebase.Timestamp.now().toDate().toString(),
                "expiresAt" to ""
            )
            val docRef = db.collection("alerts").add(data).await()
            Result.Success(
                Alert(
                    id = docRef.id,
                    title = request.title,
                    message = request.message,
                    severity = request.severity,
                    area = request.area,
                    reportedBy = request.reportedBy,
                    reportedByName = request.reportedByName
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create alert")
        }

    // ─── Users / Leaderboard ────────────────────────────────────────────────

    fun observeLeaderboard(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(externalId = doc.id)
                }
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUser(userId: String): Result<User> =
        try {
            val doc = db.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)?.copy(externalId = doc.id)
                ?: return Result.Error("User not found")
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get user")
        }

    suspend fun createOrUpdateUser(userId: String, name: String, phone: String): Result<Unit> =
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                val data = hashMapOf(
                    "name" to name,
                    "displayName" to name,
                    "phone" to phone,
                    "points" to 0,
                    "reportsCount" to 0,
                    "updatesCount" to 0,
                    "village" to "",
                    "address" to "",
                    "personalInfo" to "",
                    "badges" to emptyList<String>(),
                    "createdAt" to com.google.firebase.Timestamp.now().toDate().toString()
                )
                db.collection("users").document(userId).set(data).await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save user")
        }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> =
        try {
            db.collection("users").document(userId).update(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update profile")
        }

    suspend fun incrementUserPoints(userId: String, pointsToAdd: Int, fieldToIncrement: String? = null): Result<Unit> =
        try {
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentPoints = snapshot.getLong("points") ?: 0L
                val newPoints = currentPoints + pointsToAdd
                
                val updates = mutableMapOf<String, Any>(
                    "points" to newPoints
                )
                
                if (fieldToIncrement != null) {
                    val currentCount = snapshot.getLong(fieldToIncrement) ?: 0L
                    updates[fieldToIncrement] = currentCount + 1
                }
                
                // Badge Logic
                val currentBadges = snapshot.get("badges") as? List<*> ?: emptyList<String>()
                val newBadges = currentBadges.filterIsInstance<String>().toMutableList()
                
                val reportsCount = if (fieldToIncrement == "reportsCount") (snapshot.getLong("reportsCount") ?: 0L) + 1 else (snapshot.getLong("reportsCount") ?: 0L)
                val updatesCount = if (fieldToIncrement == "updatesCount") (snapshot.getLong("updatesCount") ?: 0L) + 1 else (snapshot.getLong("updatesCount") ?: 0L)
                
                if (reportsCount >= 1 && "First Report" !in newBadges) newBadges.add("First Report")
                if (reportsCount >= 5 && "Active Reporter" !in newBadges) newBadges.add("Active Reporter")
                if (reportsCount >= 20 && "Road Guardian" !in newBadges) newBadges.add("Road Guardian")
                if (updatesCount >= 5 && "Update Pro" !in newBadges) newBadges.add("Update Pro")
                if (newPoints >= 50 && "50 Points Club" !in newBadges) newBadges.add("50 Points Club")
                
                updates["badges"] = newBadges
                
                transaction.update(userRef, updates)
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update points")
        }

    // ─── Summary (computed client-side from reports) ─────────────────────────

    fun observeSummary(): Flow<ReportSummary> = callbackFlow {
        val listener = db.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val reports = snapshot.documents.mapNotNull { it.toObject(RoadReport::class.java) }
                val byType = reports.groupBy { it.type }.mapValues { it.value.size }
                trySend(
                    ReportSummary(
                        total = reports.size,
                        active = reports.count { it.status == "ACTIVE" },
                        inProgress = reports.count { it.status == "IN_PROGRESS" },
                        resolved = reports.count { it.status == "RESOLVED" },
                        byType = byType
                    )
                )
            }
        awaitClose { listener.remove() }
    }
}
