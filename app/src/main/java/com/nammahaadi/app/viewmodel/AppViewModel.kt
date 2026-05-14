package com.nammahaadi.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.nammahaadi.app.data.model.*
import com.nammahaadi.app.data.repository.FirestoreRepository
import com.nammahaadi.app.data.repository.Result
import com.nammahaadi.app.fcm.NammaHaadiFcmService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val application: Application,
    val firestoreRepo: FirestoreRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("namma_haadi_prefs", Context.MODE_PRIVATE)

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    val reports: StateFlow<List<RoadReport>> = combine(_selectedType, _selectedStatus) { type, status -> type to status }
        .flatMapLatest { (type, status) -> firestoreRepo.observeReports(type, status) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<ReportSummary> = firestoreRepo.observeSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportSummary())

    val alerts: StateFlow<List<Alert>> = firestoreRepo.observeAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<User>> = firestoreRepo.observeLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val currentRank: StateFlow<Int> = combine(users, _currentUser) { userList, current ->
        if (current == null) 0
        else {
            val index = userList.indexOfFirst { it.externalId == current.externalId }
            if (index != -1) index + 1 else 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _reportSubmitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val reportSubmitState: StateFlow<SubmitState> = _reportSubmitState.asStateFlow()

    private val _updateSubmitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val updateSubmitState: StateFlow<SubmitState> = _updateSubmitState.asStateFlow()

    init {
        checkUserLoggedIn()
        subscribeToFcmTopic()
    }

    private fun checkUserLoggedIn() {
        // First check Firebase Auth
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            loadCurrentUser(firebaseUser.uid)
        } else {
            // Then check Mock/Testing login persistence
            val savedUid = prefs.getString("logged_in_uid", null)
            if (savedUid != null) {
                loadCurrentUser(savedUid)
            }
        }
    }

    private fun subscribeToFcmTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(NammaHaadiFcmService.TOPIC_ROAD_ALERTS)
    }

    fun setTypeFilter(type: String?) { _selectedType.value = type }
    fun setStatusFilter(status: String?) { _selectedStatus.value = status }

    fun loadAll() { getUid()?.let { loadCurrentUser(it) } }
    fun loadAlerts() { getUid()?.let { loadCurrentUser(it) } }

    private fun getUid(): String? {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid 
            ?: prefs.getString("logged_in_uid", null)
    }

    fun loadCurrentUser(userId: String) {
        viewModelScope.launch {
            when (val result = firestoreRepo.getUser(userId)) {
                is Result.Success -> _currentUser.value = result.data
                is Result.Error -> { /* User might be new */ }
                else -> {}
            }
        }
    }

    fun onLoginSuccess(userId: String, name: String, phone: String) {
        // Save UID for persistence (Simulation Mode)
        prefs.edit().putString("logged_in_uid", userId).apply()
        
        viewModelScope.launch {
            firestoreRepo.createOrUpdateUser(userId, name, phone)
            loadCurrentUser(userId)
        }
    }

    fun updateUserProfile(updates: Map<String, Any>) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = firestoreRepo.updateUserProfile(uid, updates)) {
                is Result.Success -> loadCurrentUser(uid)
                is Result.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = firestoreRepo.uploadImage(uri, "profile_pictures")) {
                is Result.Success -> updateUserProfile(mapOf("photoUrl" to result.data))
                is Result.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun submitReport(request: CreateReportRequest) {
        viewModelScope.launch {
            _reportSubmitState.value = SubmitState.Loading
            _isLoading.value = true
            when (val result = firestoreRepo.createReport(request)) {
                is Result.Success -> {
                    _reportSubmitState.value = SubmitState.Success
                    getUid()?.let { loadCurrentUser(it) }
                }
                is Result.Error -> _reportSubmitState.value = SubmitState.Error(result.message)
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun submitUpdate(request: CreateUpdateRequest) {
        viewModelScope.launch {
            _updateSubmitState.value = SubmitState.Loading
            _isLoading.value = true
            when (val result = firestoreRepo.createReportUpdate(request)) {
                is Result.Success -> {
                    _updateSubmitState.value = SubmitState.Success
                    getUid()?.let { loadCurrentUser(it) }
                }
                is Result.Error -> _updateSubmitState.value = SubmitState.Error(result.message)
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun submitAlert(request: CreateAlertRequest) {
        viewModelScope.launch { firestoreRepo.createAlert(request) }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            prefs.edit().remove("logged_in_uid").apply()
            _currentUser.value = null
            onComplete()
        }
    }

    fun resetReportSubmitState() { _reportSubmitState.value = SubmitState.Idle }
    fun resetUpdateSubmitState() { _updateSubmitState.value = SubmitState.Idle }
    fun clearError() { _error.value = null }
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}
