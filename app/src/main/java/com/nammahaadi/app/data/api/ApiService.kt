package com.nammahaadi.app.data.api

import com.nammahaadi.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("reports")
    suspend fun getReports(
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null
    ): Response<List<RoadReport>>

    @GET("reports/stats/summary")
    suspend fun getReportsSummary(): Response<ReportSummary>

    @GET("reports/{id}")
    suspend fun getReport(@Path("id") id: String): Response<RoadReport>

    @POST("reports")
    suspend fun createReport(@Body request: CreateReportRequest): Response<RoadReport>

    @GET("report-updates")
    suspend fun getReportUpdates(@Query("reportId") reportId: String): Response<List<ReportUpdate>>

    @POST("report-updates")
    suspend fun createReportUpdate(@Body request: CreateUpdateRequest): Response<ReportUpdate>

    @GET("alerts")
    suspend fun getAlerts(): Response<List<Alert>>

    @POST("alerts")
    suspend fun createAlert(@Body request: CreateAlertRequest): Response<Alert>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>
}
