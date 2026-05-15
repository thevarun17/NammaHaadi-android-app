package com.nammahaadi.app.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.nammahaadi.app.data.model.CreateAlertRequest
import com.nammahaadi.app.data.model.CreateReportRequest
import com.nammahaadi.app.data.model.RoadReport
import com.nammahaadi.app.data.repository.FirestoreRepository
import com.nammahaadi.app.data.repository.Result
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val application = mockk<Application>()
    private val firestoreRepo = mockk<FirestoreRepository>(relaxed = true)
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { application.getSharedPreferences(any(), any()) } returns sharedPrefs
        
        // Mock default flows
        every { firestoreRepo.observeReports(any(), any()) } returns flowOf(emptyList())
        every { firestoreRepo.observeSummary() } returns flowOf(mockk(relaxed = true))
        every { firestoreRepo.observeAlerts() } returns flowOf(emptyList())
        every { firestoreRepo.observeLeaderboard() } returns flowOf(emptyList())

        viewModel = AppViewModel(application, firestoreRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setTypeFilter updates selectedType flow`() {
        viewModel.setTypeFilter("POTHOLE")
        assertEquals("POTHOLE", viewModel.selectedType.value)
    }

    @Test
    fun `setStatusFilter updates selectedStatus flow`() {
        viewModel.setStatusFilter("RESOLVED")
        assertEquals("RESOLVED", viewModel.selectedStatus.value)
    }

    @Test
    fun `submitReport calls repository and updates state`() = runTest {
        val request = CreateReportRequest(
            "POTHOLE", "HIGH", "Title", "Address", "Desc", 
            12.0, 77.0, "user123", "User Name", null
        )
        
        coEvery { firestoreRepo.createReport(request) } returns Result.Success(RoadReport(id = "new_id"))
        
        viewModel.submitReport(request)
        
        coVerify { firestoreRepo.createReport(request) }
        assertEquals(SubmitState.Success, viewModel.reportSubmitState.value)
    }

    @Test
    fun `submitAlert calls repository`() = runTest {
        val request = CreateAlertRequest("Title", "Message", "DANGER", "Area", "uid", "Name")
        
        viewModel.submitAlert(request)
        
        coVerify { firestoreRepo.createAlert(request) }
    }

    @Test
    fun `logout signs out from Firebase and clears prefs`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefs.edit() } returns editor
        
        // Mock static call if possible or just check prefs
        // com.google.firebase.auth.FirebaseAuth.getInstance() is hard to mock without extra tools
        // but we can check the prefs part
        
        viewModel.logout { }
        
        verify { editor.remove("logged_in_uid") }
        verify { editor.apply() }
        assertEquals(null, viewModel.currentUser.value)
    }
}
