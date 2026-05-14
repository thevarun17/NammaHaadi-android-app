package com.nammahaadi.app.data.repository

import com.nammahaadi.app.data.api.ApiService
import com.nammahaadi.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class AppRepository @Inject constructor(private val api: ApiService) {

    suspend fun getReports(type: String? = null, status: String? = null): Result<List<RoadReport>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getReports(type, status)
                if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
                else Result.Error("Server error: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun getReportsSummary(): Result<ReportSummary> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getReportsSummary()
                if (response.isSuccessful) Result.Success(response.body() ?: ReportSummary())
                else Result.Error("Server error: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun createReport(request: CreateReportRequest): Result<RoadReport> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createReport(request)
                if (response.isSuccessful) Result.Success(response.body()!!)
                else Result.Error("Failed to submit: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun getReportUpdates(reportId: String): Result<List<ReportUpdate>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getReportUpdates(reportId)
                if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
                else Result.Error("Server error: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun createReportUpdate(request: CreateUpdateRequest): Result<ReportUpdate> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createReportUpdate(request)
                if (response.isSuccessful) Result.Success(response.body()!!)
                else Result.Error("Failed to submit: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun getAlerts(): Result<List<Alert>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAlerts()
                if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
                else Result.Error("Server error: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun createAlert(request: CreateAlertRequest): Result<Alert> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createAlert(request)
                if (response.isSuccessful) Result.Success(response.body()!!)
                else Result.Error("Failed: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun getUsers(): Result<List<User>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUsers()
                if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
                else Result.Error("Server error: ${response.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun getUser(id: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUser(id)
                if (response.isSuccessful) Result.Success(response.body()!!)
                else Result.Error("User not found")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }
}
