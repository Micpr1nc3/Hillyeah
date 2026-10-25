package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.chatbot.HikingChatbotService
import com.example.data.local.AppDatabase
import com.example.data.model.ArchetypeResult
import com.example.data.model.ChatMessage
import com.example.data.model.DailyPulseCheckIn
import com.example.data.model.DailyPulseInterpretation
import com.example.data.model.DailyPulseQuestion
import com.example.data.model.SuggestedActivityType
import com.example.data.model.Hike
import com.example.data.model.HikeWithDetails
import com.example.data.model.HillYeahConstants
import com.example.data.model.InboxMessage
import com.example.data.model.OfflineTrailMap
import com.example.data.model.Trail
import com.example.data.model.User
import com.example.data.model.WeatherReport
import com.example.data.model.WellnessOption
import com.example.data.model.WellnessQuestion
import com.example.data.repository.DashboardHikes
import com.example.data.repository.HillYeahRepository
import com.example.data.wellness.DailyPulseData
import com.example.data.wellness.WeeklyTherapistReport
import com.example.data.wellness.WeeklyTherapistService
import com.example.data.wellness.WellnessMasterPool
import com.example.ui.navigation.Screen
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HillYeahViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = HillYeahRepository(database)

    // Navigation state stack — initialized to Screen.Welcome for mandatory auth gate
    private val _navigationStack = MutableStateFlow<List<Screen>>(listOf(Screen.Welcome))
    val currentScreen: StateFlow<Screen> = _navigationStack
        .map { it.lastOrNull() ?: Screen.Welcome }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Welcome)

    // User State
    val currentUser: StateFlow<User?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: StateFlow<Boolean> = currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Auth Required Interception Modal
    val showAuthPrompt = MutableStateFlow(false)
    val authPromptMessage = MutableStateFlow("Please log in or create an account to access this feature.")
    private var pendingAuthAction: (() -> Unit)? = null

    // Toast events
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Trails Filtering State
    val trailSearchQuery = MutableStateFlow("")
    val selectedProvinceFilter = MutableStateFlow("All")
    val selectedDifficultyFilter = MutableStateFlow("All")
    val waterfallFilterOnly = MutableStateFlow(false)

    val trails: StateFlow<List<Trail>> = repository.trailsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTrails: StateFlow<List<Trail>> = combine(
        trails,
        trailSearchQuery,
        selectedProvinceFilter,
        selectedDifficultyFilter,
        waterfallFilterOnly
    ) { allTrails, query, province, difficulty, waterfallOnly ->
        allTrails.filter { trail ->
            val matchesQuery = query.isBlank() ||
                trail.name.contains(query, ignoreCase = true) ||
                trail.location.contains(query, ignoreCase = true) ||
                trail.province.contains(query, ignoreCase = true) ||
                trail.description.contains(query, ignoreCase = true)

            val matchesProvince = province == "All" || trail.province.equals(province, ignoreCase = true)
            val matchesDifficulty = difficulty == "All" || trail.difficulty.equals(difficulty, ignoreCase = true)
            val matchesWaterfall = !waterfallOnly || trail.hasWaterfall

            matchesQuery && matchesProvince && matchesDifficulty && matchesWaterfall
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hikes Filtering State
    val hikeProvinceFilter = MutableStateFlow("All")
    val allHikes: StateFlow<List<HikeWithDetails>> = repository.getAllHikesWithDetailsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHikes: StateFlow<List<HikeWithDetails>> = combine(
        allHikes,
        hikeProvinceFilter
    ) { hikes, province ->
        hikes.filter { item ->
            province == "All" || item.hike.province.equals(province, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard State
    val dashboardHikes: StateFlow<DashboardHikes> = repository.getDashboardHikesFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardHikes(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        )

    // Current selected hike ID for details screen
    val selectedHikeId = MutableStateFlow<String?>(null)
    val selectedHikeDetail: StateFlow<HikeWithDetails?> = combine(
        allHikes,
        selectedHikeId
    ) { hikes, id ->
        if (id == null) null else hikes.find { it.hike.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current selected trail for details screen
    val selectedTrailId = MutableStateFlow<String?>(null)
    val selectedTrailDetail: StateFlow<Trail?> = combine(
        trails,
        selectedTrailId
    ) { allTrails, id ->
        if (id == null) null else allTrails.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Member Public Profile State
    val selectedMemberId = MutableStateFlow<String?>(null)
    val selectedMemberUser: StateFlow<User?> = selectedMemberId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getUserByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedMemberHikes: StateFlow<DashboardHikes> = selectedMemberId.flatMapLatest { id ->
        if (id == null) flowOf(DashboardHikes(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
        else repository.getMemberHikesFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardHikes(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))

    // Host Profile Inbox Messages
    val inboxMessages: StateFlow<List<InboxMessage>> = repository.inboxMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offline Room Caching State
    val allOfflineTrails: StateFlow<List<OfflineTrailMap>> = repository.getAllOfflineTrailsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isCachingTrail = MutableStateFlow(false)

    val selectedTrailOfflineMap: StateFlow<OfflineTrailMap?> = combine(
        allOfflineTrails,
        selectedTrailId
    ) { cachedMaps, id ->
        if (id == null) null else cachedMaps.find { it.trailId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSelectedTrailCached: StateFlow<Boolean> = combine(
        allOfflineTrails,
        selectedTrailId
    ) { cachedMaps, id ->
        if (id == null) false else cachedMaps.any { it.trailId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ---------------------------------------------------------
    // DASHBOARD QUOTES, TIME & WEATHER
    // ---------------------------------------------------------

    private val _quoteIndex = MutableStateFlow(0)
    val currentQuoteIndex: StateFlow<Int> = _quoteIndex.asStateFlow()

    private val _isOfflineFixedQuote = MutableStateFlow(false)
    val isOfflineFixedQuote: StateFlow<Boolean> = _isOfflineFixedQuote.asStateFlow()

    fun nextQuote() {
        _quoteIndex.value = (_quoteIndex.value + 1) % HillYeahConstants.HikingQuotes.size
    }

    fun toggleOfflineFixedQuote() {
        _isOfflineFixedQuote.value = !_isOfflineFixedQuote.value
        val msg = if (_isOfflineFixedQuote.value) {
            "Quote locked in Offline Mode."
        } else {
            "Quotes set to auto-update constantly."
        }
        viewModelScope.launch { _toastEvent.emit(msg) }
    }

    // Weather state based on user's province/city
    val selectedWeatherProvince = MutableStateFlow("Western Cape")

    private val regionalWeatherMap = mapOf(
        "Western Cape" to WeatherReport("Cape Town", "Western Cape", 22, "Sunny & Crisp", 24, 14, 16, 62, "Moderate 5", "Ideal for Table Mountain & Lion's Head"),
        "Gauteng" to WeatherReport("Johannesburg", "Gauteng", 24, "Partly Cloudy", 26, 13, 14, 42, "High 7", "Perfect morning for Hennops or Walter Sisulu"),
        "KwaZulu-Natal" to WeatherReport("Royal Natal / Drakensberg", "KwaZulu-Natal", 20, "Crisp Mountain Breeze", 23, 11, 10, 58, "Low 3", "Great hiking condition; pack wind shell"),
        "Mpumalanga" to WeatherReport("Blyde River Canyon / Graskop", "Mpumalanga", 23, "Mild & Clear", 25, 14, 8, 70, "Moderate 6", "Waterfalls flowing strong; ideal for hiking"),
        "Eastern Cape" to WeatherReport("Tsitsikamma / Gqeberha", "Eastern Cape", 21, "Fresh Sea Breeze", 22, 13, 18, 76, "Moderate 5", "Great coastal trail conditions"),
        "Free State" to WeatherReport("Golden Gate / Clarens", "Free State", 19, "Crisp Highveld Sun", 21, 8, 12, 48, "Moderate 6", "Cool mountain hiking weather"),
        "Limpopo" to WeatherReport("Magoebaskloof / Tzaneen", "Limpopo", 21, "Morning Mountain Mist", 23, 14, 6, 82, "Low 4", "Lush rainforest waterfalls active"),
        "North West" to WeatherReport("Magaliesberg / Rustenburg", "North West", 25, "Warm & Clear", 27, 15, 10, 38, "High 7", "Great for Castle Gorge; carry 2L water"),
        "Northern Cape" to WeatherReport("Augrabies Falls / Kakamas", "Northern Cape", 28, "Sunny & Dry", 31, 16, 12, 28, "High 8", "Start early morning; bring 3L water")
    )

    val currentWeather: StateFlow<WeatherReport> = selectedWeatherProvince.map { prov ->
        regionalWeatherMap[prov] ?: regionalWeatherMap["Western Cape"]!!
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        regionalWeatherMap["Western Cape"]!!
    )

    fun selectWeatherProvince(province: String) {
        selectedWeatherProvince.value = province
    }

    // ---------------------------------------------------------
    // HIKING CHATBOT STATE (SAGE)
    // ---------------------------------------------------------

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Sawubona! I'm Sage, your HillYeah hiking companion. Ask me anything about South African trails, waterfall walks, gear, baboon safety, or weather conditions!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendChatMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _isChatLoading.value) return

        val userMessage = ChatMessage(text = trimmed, isUser = true)
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMessage)
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isChatLoading.value = true
            val loc = currentUser.value?.let { "${it.city}, ${it.province}" } ?: selectedWeatherProvince.value
            val responseText = HikingChatbotService.getResponse(trimmed, loc)
            _isChatLoading.value = false

            val sageMessage = ChatMessage(text = responseText, isUser = false)
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(sageMessage)
            _chatMessages.value = updatedList
        }
    }

    // Navigation functions
    fun navigateTo(screen: Screen) {
        val currentList = _navigationStack.value.toMutableList()
        currentList.add(screen)
        _navigationStack.value = currentList

        when (screen) {
            is Screen.HikeDetail -> selectedHikeId.value = screen.hikeId
            is Screen.TrailDetail -> selectedTrailId.value = screen.trailId
            is Screen.MemberProfile -> selectedMemberId.value = screen.userId
            else -> {}
        }
    }

    fun navigateBack(): Boolean {
        val currentList = _navigationStack.value.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _navigationStack.value = currentList
            return true
        }
        return false
    }

    fun navigateRoot(screen: Screen) {
        _navigationStack.value = listOf(screen)
        when (screen) {
            is Screen.HikeDetail -> selectedHikeId.value = screen.hikeId
            is Screen.TrailDetail -> selectedTrailId.value = screen.trailId
            is Screen.MemberProfile -> selectedMemberId.value = screen.userId
            else -> {}
        }
    }

    fun handleIncomingInvite(hikeId: String, hostId: String? = null) {
        val trimmed = hikeId.trim()
        if (trimmed.isNotBlank()) {
            _navigationStack.value = listOf(Screen.InviteResponse(trimmed, hostId?.trim()?.takeIf { it.isNotBlank() }))
        }
    }

    // ---------------------------------------------------------
    // AUTHENTICATION ACTIONS & GATEKEEPING
    // ---------------------------------------------------------

    fun requireAuth(reason: String = "Please log in to continue.", onAuthenticated: () -> Unit) {
        if (currentUser.value != null) {
            onAuthenticated()
        } else {
            pendingAuthAction = onAuthenticated
            authPromptMessage.value = reason
            showAuthPrompt.value = true
        }
    }

    fun dismissAuthPrompt() {
        showAuthPrompt.value = false
        pendingAuthAction = null
    }

    private fun executePendingAuthAction() {
        val action = pendingAuthAction
        pendingAuthAction = null
        showAuthPrompt.value = false
        action?.invoke()
    }

    fun login(
        email: String,
        password: String,
        onFailure: ((String) -> Unit)? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess { user ->
                _toastEvent.emit("Welcome back, ${user.name}!")
                onSuccess()
                executePendingAuthAction()
            }.onFailure { err ->
                val msg = err.message ?: "Login failed. Please check your credentials."
                _toastEvent.emit(msg)
                onFailure?.invoke(msg)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        avatarUrl: String?,
        onFailure: ((String) -> Unit)? = null,
        onSuccess: () -> Unit
    ) {
        if (password != confirmPassword) {
            val msg = "Passwords do not match. Please verify and try again."
            viewModelScope.launch {
                _toastEvent.emit(msg)
            }
            onFailure?.invoke(msg)
            return
        }

        viewModelScope.launch {
            val result = repository.registerUser(
                name = name,
                email = email,
                password = password,
                avatarUrl = avatarUrl
            )
            result.onSuccess { user ->
                _toastEvent.emit("Welcome to HillYeah, ${user.name}!")
                onSuccess()
                executePendingAuthAction()
            }.onFailure { err ->
                val msg = err.message ?: "Registration failed."
                _toastEvent.emit(msg)
                onFailure?.invoke(msg)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _toastEvent.emit("You have logged out of HillYeah.")
            navigateRoot(Screen.Welcome)
        }
    }

    fun loginWithSocial(
        provider: String,
        email: String,
        name: String,
        avatarUrl: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginWithSocial(provider, email, name, avatarUrl)
            result.onSuccess { user ->
                _toastEvent.emit("Welcome, ${user.name}! Signed in via $provider.")
                onSuccess()
                executePendingAuthAction()
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Social sign-in failed.")
            }
        }
    }

    fun loginWithMagicLink(
        email: String,
        onSuccess: () -> Unit,
        onFailure: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.loginWithMagicLink(email)
            result.onSuccess { user ->
                _toastEvent.emit("✨ Magic link verified! Welcome, ${user.name}.")
                onSuccess()
                executePendingAuthAction()
            }.onFailure { err ->
                val msg = err.message ?: "Failed to sign in with magic link."
                _toastEvent.emit(msg)
                onFailure?.invoke(msg)
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, confirmPassword: String, onSuccess: () -> Unit) {
        if (newPassword != confirmPassword) {
            viewModelScope.launch {
                _toastEvent.emit("Passwords do not match.")
            }
            return
        }
        viewModelScope.launch {
            val result = repository.resetPassword(email, newPassword)
            result.onSuccess {
                _toastEvent.emit("Password reset successful. Please log in with your new password.")
                onSuccess()
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Failed to reset password")
            }
        }
    }

    // ---------------------------------------------------------
    // HIKES, RSVP & SHARED INVITE RESPONSE
    // ---------------------------------------------------------

    fun submitRsvp(hikeId: String, status: String, guestsCount: Int) {
        requireAuth("Please log in or register to RSVP to this group hike.") {
            viewModelScope.launch {
                val result = repository.submitRsvp(hikeId, status, guestsCount)
                result.onSuccess { message ->
                    _toastEvent.emit(message)
                }.onFailure { err ->
                    _toastEvent.emit(err.message ?: "Failed to update RSVP")
                }
            }
        }
    }

    fun respondToSharedInvite(
        hikeId: String,
        hostId: String,
        action: String,
        notes: String = "",
        guestName: String? = null,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.respondToHikeInvite(
                hikeId = hikeId,
                recipientHostId = hostId,
                action = action,
                notes = notes,
                guestName = guestName
            )
            result.onSuccess { confirmText ->
                _toastEvent.emit(confirmText)
                onSuccess(confirmText)
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Failed to record response")
            }
        }
    }

    fun markInboxMessageAsRead(id: String) {
        viewModelScope.launch {
            repository.markInboxMessageAsRead(id)
        }
    }

    fun deleteInboxMessage(id: String) {
        viewModelScope.launch {
            repository.deleteInboxMessage(id)
            _toastEvent.emit("Message removed from Inbox.")
        }
    }

    fun cancelHike(hikeId: String) {
        viewModelScope.launch {
            val result = repository.cancelHike(hikeId)
            result.onSuccess {
                _toastEvent.emit("Hike cancelled. Attendees will be notified.")
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Could not cancel hike")
            }
        }
    }

    fun createHike(
        title: String,
        trailId: String?,
        customTrailName: String,
        province: String,
        location: String,
        date: String,
        time: String,
        meetingPoint: String,
        difficulty: String,
        distanceKm: Double,
        capacity: Int,
        description: String,
        onSuccess: (Hike) -> Unit
    ) {
        requireAuth("Please log in or register to host a group hike on HillYeah.") {
            viewModelScope.launch {
                val result = repository.createHike(
                    title = title,
                    trailId = trailId,
                    customTrailName = customTrailName,
                    province = province,
                    location = location,
                    date = date,
                    time = time,
                    meetingPoint = meetingPoint,
                    difficulty = difficulty,
                    distanceKm = distanceKm,
                    capacity = capacity,
                    description = description
                )
                result.onSuccess { hike ->
                    _toastEvent.emit("🎉 Hike successfully published!")
                    onSuccess(hike)
                }.onFailure { err ->
                    _toastEvent.emit(err.message ?: "Error publishing hike")
                }
            }
        }
    }

    fun updateHike(
        hikeId: String,
        title: String,
        trailName: String,
        location: String,
        province: String,
        date: String,
        time: String,
        meetingPoint: String,
        difficulty: String,
        distanceKm: Double,
        capacity: Int,
        description: String,
        onSuccess: (Hike) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateHike(
                hikeId = hikeId,
                title = title,
                trailName = trailName,
                location = location,
                province = province,
                date = date,
                time = time,
                meetingPoint = meetingPoint,
                difficulty = difficulty,
                distanceKm = distanceKm,
                capacity = capacity,
                description = description
            )
            result.onSuccess { updated ->
                _toastEvent.emit("Hike details updated: ${updated.title}")
                onSuccess(updated)
            }.onFailure { err ->
                val msg = err.message ?: "Failed to update hike."
                _toastEvent.emit(msg)
                onFailure(msg)
            }
        }
    }

    // ---------------------------------------------------------
    // PROFILE MANAGEMENT
    // ---------------------------------------------------------

    fun updateProfile(
        name: String,
        city: String,
        province: String,
        experienceLevel: String,
        interests: List<String>,
        bio: String,
        avatarUrl: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(
                name = name.trim(),
                location = if (city.isNotBlank()) "$city, $province" else province,
                city = city.trim(),
                province = province,
                experienceLevel = experienceLevel,
                interests = interests,
                bio = bio.trim(),
                avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else user.avatarUrl
            )
            repository.updateCurrentUser(updated)
            _toastEvent.emit("Profile updated successfully!")
        }
    }

    fun viewMemberProfile(userId: String) {
        selectedMemberId.value = userId
        navigateTo(Screen.MemberProfile(userId))
    }

    // ---------------------------------------------------------
    // OFFLINE CACHING
    // ---------------------------------------------------------

    fun cacheTrail(trail: Trail) {
        viewModelScope.launch {
            isCachingTrail.value = true
            val result = repository.cacheTrailForOffline(trail)
            isCachingTrail.value = false
            result.onSuccess { map ->
                val sizeMb = String.format(java.util.Locale.US, "%.1f", map.cacheSizeBytes / (1024.0 * 1024.0))
                _toastEvent.emit("Cached ${trail.name} map & GPS waypoints to Room (${sizeMb} MB)")
            }.onFailure { err ->
                _toastEvent.emit("Failed to save offline map: ${err.message}")
            }
        }
    }

    fun removeOfflineTrail(trailId: String) {
        viewModelScope.launch {
            val result = repository.removeOfflineTrail(trailId)
            result.onSuccess {
                _toastEvent.emit("Removed trail map and waypoints from offline cache")
            }.onFailure { err ->
                _toastEvent.emit("Could not remove offline cache: ${err.message}")
            }
        }
    }

    // ---------------------------------------------------------
    // ---------------------------------------------------------
    // HILL YEAH — DAILY PULSE WELLNESS FEATURE
    // ---------------------------------------------------------

    private val wellnessPrefs = getApplication<Application>().getSharedPreferences("hillyeah_wellness_prefs", Context.MODE_PRIVATE)

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getActiveUserId(): String {
        return currentUser.value?.id ?: "guest_user"
    }

    val dailyPulseQuestions: List<DailyPulseQuestion> = DailyPulseData.questions

    private val _isDailyPulseActive = MutableStateFlow(false)
    val isDailyPulseActive: StateFlow<Boolean> = _isDailyPulseActive.asStateFlow()

    private val _currentDailyPulseIndex = MutableStateFlow(0)
    val currentDailyPulseIndex: StateFlow<Int> = _currentDailyPulseIndex.asStateFlow()

    private val _currentDailyPulseAnswers = MutableStateFlow<Map<Int, String?>>(emptyMap())
    val currentDailyPulseAnswers: StateFlow<Map<Int, String?>> = _currentDailyPulseAnswers.asStateFlow()

    private val _todayDailyPulse = MutableStateFlow<DailyPulseCheckIn?>(null)
    val todayDailyPulse: StateFlow<DailyPulseCheckIn?> = _todayDailyPulse.asStateFlow()

    private val _dailyPulseHistory = MutableStateFlow<List<DailyPulseCheckIn>>(emptyList())
    val dailyPulseHistory: StateFlow<List<DailyPulseCheckIn>> = _dailyPulseHistory.asStateFlow()

    private val _hasCheckedInToday = MutableStateFlow(false)
    val hasCheckedInToday: StateFlow<Boolean> = _hasCheckedInToday.asStateFlow()

    // Backwards-compatible assessing flag
    val isAssessing: StateFlow<Boolean> = _isDailyPulseActive.asStateFlow()

    fun loadDailyPulseForCurrentUser() {
        val uid = getActiveUserId()
        val todayStr = getTodayDateString()

        val historyJson = wellnessPrefs.getString("daily_pulse_history_$uid", "[]") ?: "[]"
        val history = DailyPulseData.deserializeCheckInList(historyJson)
        _dailyPulseHistory.value = history

        val lastDate = wellnessPrefs.getString("daily_pulse_last_date_$uid", "") ?: ""
        val todaySavedJson = wellnessPrefs.getString("daily_pulse_today_$uid", null)
        val todayCheckIn = if (todaySavedJson != null && lastDate == todayStr) {
            DailyPulseData.deserializeCheckIn(todaySavedJson)
        } else {
            history.firstOrNull { it.date == todayStr }
        }

        _todayDailyPulse.value = todayCheckIn
        _hasCheckedInToday.value = (todayCheckIn != null)
        // If user already checked in today, do not force them into questionnaire
        _isDailyPulseActive.value = false
    }

    fun startDailyPulse(reset: Boolean = false) {
        _currentDailyPulseIndex.value = 0
        val existingAnswers = if (reset && _todayDailyPulse.value != null) {
            _todayDailyPulse.value!!.answers
        } else {
            emptyMap()
        }
        _currentDailyPulseAnswers.value = existingAnswers
        _isDailyPulseActive.value = true
    }

    fun cancelDailyPulse() {
        _isDailyPulseActive.value = false
    }

    fun selectDailyPulseAnswer(questionId: Int, answer: String) {
        val updated = _currentDailyPulseAnswers.value.toMutableMap()
        updated[questionId] = answer
        _currentDailyPulseAnswers.value = updated
    }

    fun goToPreviousQuestion() {
        if (_currentDailyPulseIndex.value > 0) {
            _currentDailyPulseIndex.value -= 1
        }
    }

    fun skipCurrentQuestion() {
        val qId = dailyPulseQuestions[_currentDailyPulseIndex.value].id
        val updated = _currentDailyPulseAnswers.value.toMutableMap()
        if (!updated.containsKey(qId)) {
            updated[qId] = null
        }
        _currentDailyPulseAnswers.value = updated
        advanceOrSubmit()
    }

    fun advanceOrSubmit() {
        if (_currentDailyPulseIndex.value < dailyPulseQuestions.size - 1) {
            _currentDailyPulseIndex.value += 1
        } else {
            submitDailyPulse()
        }
    }

    fun submitDailyPulse() {
        val answers = _currentDailyPulseAnswers.value
        val interpretation = DailyPulseData.interpret(answers)
        val todayStr = getTodayDateString()
        val uid = getActiveUserId()

        val checkIn = DailyPulseCheckIn(
            id = UUID.randomUUID().toString(),
            userId = uid,
            date = todayStr,
            timestamp = System.currentTimeMillis(),
            answers = answers,
            interpretation = interpretation
        )

        // Update history (replace today's if already present, or prepend)
        val existingHistory = _dailyPulseHistory.value.filter { it.date != todayStr }.toMutableList()
        existingHistory.add(0, checkIn)

        // Persist to user's SharedPreferences account
        wellnessPrefs.edit()
            .putString("daily_pulse_last_date_$uid", todayStr)
            .putString("daily_pulse_today_$uid", DailyPulseData.serializeCheckIn(checkIn))
            .putString("daily_pulse_history_$uid", DailyPulseData.serializeCheckInList(existingHistory))
            .apply()

        _todayDailyPulse.value = checkIn
        _dailyPulseHistory.value = existingHistory
        _hasCheckedInToday.value = true
        _isDailyPulseActive.value = false

        viewModelScope.launch {
            _toastEvent.emit("Wellness check-in saved: ${interpretation.gentleHeadline}")
        }
    }

    /**
     * Resets today's check-in, clearing today's record from preferences and history,
     * allowing the user to start a completely fresh check-in.
     */
    fun resetTodayDailyPulse() {
        val uid = getActiveUserId()
        val todayStr = getTodayDateString()
        val existingHistory = _dailyPulseHistory.value.filter { it.date != todayStr }

        wellnessPrefs.edit()
            .remove("daily_pulse_today_$uid")
            .putString("daily_pulse_history_$uid", DailyPulseData.serializeCheckInList(existingHistory))
            .apply()

        _todayDailyPulse.value = null
        _dailyPulseHistory.value = existingHistory
        _hasCheckedInToday.value = false
        _currentDailyPulseAnswers.value = emptyMap()
        _currentDailyPulseIndex.value = 0
        _isDailyPulseActive.value = false

        viewModelScope.launch {
            _toastEvent.emit("Wellness check-in reset. You can take a fresh check-in anytime.")
        }
    }

    /**
     * Retakes today's check-in, allowing the user to update their responses.
     */
    fun retakeDailyPulse() {
        startDailyPulse(reset = true)
    }

    // Synthesizes weekly check-ins with modern therapist clinical intelligence
    val weeklyTherapistReport: StateFlow<WeeklyTherapistReport> = combine(
        dailyPulseHistory,
        currentUser
    ) { history, user ->
        WeeklyTherapistService.synthesizeWeeklyReport(history, user)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyTherapistService.synthesizeWeeklyReport(emptyList(), null)
    )

    // Curated dynamic trail recommendations matching user's energy and what helps
    val dailyPulseTrails: StateFlow<List<Trail>> = combine(
        trails,
        todayDailyPulse
    ) { allTrails, checkIn ->
        if (checkIn == null) {
            allTrails.filter { it.difficulty == "Easy" || it.hasWaterfall }.take(4)
        } else {
            when (checkIn.interpretation.suggestedActivityType) {
                SuggestedActivityType.REST_AND_RECHARGE -> {
                    // Respect rest: do not push strenuous hikes
                    emptyList()
                }
                SuggestedActivityType.GENTLE_NATURE_WALK,
                SuggestedActivityType.QUIET_OUTDOOR_MOMENT -> {
                    allTrails.filter {
                        (it.difficulty == "Easy" || it.distanceKm <= 5.5) && (it.hasWaterfall || it.elevationM < 300)
                    }.take(4)
                }
                SuggestedActivityType.SOLO_PEACEFUL_HIKE,
                SuggestedActivityType.MIND_CLEARING_STROLL -> {
                    allTrails.filter {
                        it.distanceKm in 4.0..10.0 && it.difficulty != "Challenging"
                    }.take(4)
                }
                SuggestedActivityType.GROUP_OR_BUDDY_HIKE -> {
                    allTrails.filter { it.difficulty != "Challenging" }.take(4)
                }
                SuggestedActivityType.SMALL_GOAL_FOCUS -> {
                    allTrails.filter { it.difficulty == "Easy" || it.difficulty == "Moderate" }.take(4)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Backwards compatibility mappings for older callers
    val activePrescriptionTrails: StateFlow<List<Trail>> = dailyPulseTrails
    val activeArchetype: StateFlow<ArchetypeResult> = MutableStateFlow(WellnessMasterPool.archetypes["cruiser"]!!)
    val selectedQuestions: StateFlow<List<WellnessQuestion>> = MutableStateFlow(WellnessMasterPool.questions.take(5))
    val currentQuestionIndex: StateFlow<Int> = _currentDailyPulseIndex.asStateFlow()

    fun recalibrateWellnessCheckIn() {
        startDailyPulse(reset = true)
    }

    fun submitWellnessAnswer(option: WellnessOption) {
        advanceOrSubmit()
    }

    init {
        // Automatically cycle hiking quotes every 10 seconds unless fixed offline
        viewModelScope.launch {
            while (true) {
                delay(10_000L)
                if (!_isOfflineFixedQuote.value) {
                    val nextIdx = (_quoteIndex.value + 1) % HillYeahConstants.HikingQuotes.size
                    _quoteIndex.value = nextIdx
                }
            }
        }

        // Self-heal and repair any legacy hikes where title or trail was saved as "the"
        viewModelScope.launch {
            repository.repairInvalidHikes()
        }

        // Load user-specific Daily Pulse history when user state changes & enforce auth gating
        viewModelScope.launch {
            currentUser.collect { user ->
                loadDailyPulseForCurrentUser()
                if (user != null) {
                    val current = _navigationStack.value.lastOrNull()
                    if (current == null || current is Screen.Welcome) {
                        _navigationStack.value = listOf(Screen.Home)
                    }
                } else {
                    val current = _navigationStack.value.lastOrNull()
                    if (current != null && current !is Screen.Welcome && current !is Screen.Login && current !is Screen.Register && current !is Screen.InviteResponse) {
                        _navigationStack.value = listOf(Screen.Welcome)
                    }
                }
            }
        }
    }
}
