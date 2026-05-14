package com.nammahaadi.app.data.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class RoadReport(
    val id: String = "",
    val type: String = "",
    val severity: String = "",
    val title: String = "",
    val address: String = "",
    val description: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val status: String = "ACTIVE",
    val photoUrl: String? = null,
    val reportedBy: String = "",
    val reportedByName: String = "",
    val reportCount: Int = 1,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
) {
    fun getFormattedCreatedAt(): String = formatFirestoreDate(createdAt)
}

data class ReportSummary(
    val total: Int = 0,
    val active: Int = 0,
    val inProgress: Int = 0,
    val resolved: Int = 0,
    val byType: Map<String, Int> = emptyMap()
)

data class CreateReportRequest(
    val type: String,
    val severity: String,
    val title: String,
    val address: String,
    val description: String?,
    val lat: Double,
    val lng: Double,
    val reportedBy: String,
    val reportedByName: String,
    val photoUrl: String?
)

data class ReportUpdate(
    val id: String = "",
    val reportId: String = "",
    val reportedBy: String = "",
    val reportedByName: String = "",
    val newStatus: String = "",
    val note: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val photoUrl: String? = null,
    val createdAt: Any? = null
)

data class CreateUpdateRequest(
    val reportId: String,
    val reportedBy: String,
    val reportedByName: String,
    val newStatus: String,
    val note: String?,
    val lat: Double?,
    val lng: Double?,
    val photoUrl: String?
)

data class Alert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val severity: String = "INFO",
    val area: String = "",
    val reportedBy: String = "",
    val reportedByName: String = "",
    val createdAt: Any? = null,
    val expiresAt: String = ""
)

data class CreateAlertRequest(
    val title: String,
    val message: String,
    val severity: String,
    val area: String,
    val reportedBy: String,
    val reportedByName: String
)

data class User(
    val id: String = "",
    val externalId: String = "",
    val name: String = "",
    val displayName: String = "",
    val village: String = "",
    val address: String = "",
    val personalInfo: String = "",
    val phone: String? = null,
    val points: Int = 0,
    val reportsCount: Int = 0,
    val updatesCount: Int = 0,
    val rank: Int = 0,
    val createdAt: Any? = null,
    val badges: List<String> = emptyList(),
    val photoUrl: String? = null
)

fun formatFirestoreDate(date: Any?): String {
    return when (date) {
        is Timestamp -> date.toDate().toString()
        is String -> date
        else -> ""
    }
}

data class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null
)

enum class IssueType(val key: String, val label: String, val emoji: String, val colorHex: String) {
    POTHOLE("POTHOLE", "Pothole", "🕳️", "#E53935"),
    FLOODED("FLOODED", "Flooded Road", "🌊", "#1E88E5"),
    ROAD_WORK("ROAD_WORK", "Road Work", "🚧", "#F57C00"),
    PIPELINE("PIPELINE", "Pipeline Work", "🔧", "#7B1FA2"),
    BLOCKED("BLOCKED", "Blocked", "🚫", "#D32F2F"),
    MUDDY("MUDDY", "Muddy Road", "🚜", "#795548"),
    DRY("DRY", "Dry/Dusty", "💨", "#9E9E9E"),
    OTHER("OTHER", "Other", "⚠️", "#37474F");

    companion object {
        fun fromKey(key: String) = values().find { it.key == key } ?: OTHER
    }
}

enum class ReportStatus(val key: String, val label: String) {
    ACTIVE("ACTIVE", "Active"),
    IN_PROGRESS("IN_PROGRESS", "In Progress"),
    RESOLVED("RESOLVED", "Resolved");

    companion object {
        fun fromKey(key: String) = values().find { it.key == key } ?: ACTIVE
    }
}

enum class Severity(val key: String, val label: String) {
    LOW("LOW", "Low"),
    MEDIUM("MEDIUM", "Medium"),
    HIGH("HIGH", "High"),
    CRITICAL("CRITICAL", "Critical");

    companion object {
        fun fromKey(key: String) = values().find { it.key == key } ?: MEDIUM
    }
}
