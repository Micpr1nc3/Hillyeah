package com.example.data.repository

import com.example.data.local.ActiveSessionEntity
import com.example.data.local.AppDatabase
import com.example.data.local.HikeEntity
import com.example.data.local.InboxMessageEntity
import com.example.data.local.OfflineTrailMapEntity
import com.example.data.local.RsvpEntity
import com.example.data.local.UserEntity
import com.example.data.model.ElevationPoint
import com.example.data.model.Hike
import com.example.data.model.HikeWithDetails
import com.example.data.model.InboxMessage
import com.example.data.model.OfflineTrailMap
import com.example.data.model.Rsvp
import com.example.data.model.Trail
import com.example.data.model.TrailWaypoint
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DashboardHikes(
    val upcoming: List<HikeWithDetails>,
    val hosting: List<HikeWithDetails>,
    val joined: List<HikeWithDetails>,
    val completed: List<HikeWithDetails>,
    val cancelled: List<HikeWithDetails>
)

class HillYeahRepository(private val db: AppDatabase) {

    private val trailDao = db.trailDao()
    private val userDao = db.userDao()
    private val sessionDao = db.sessionDao()
    private val hikeDao = db.hikeDao()
    private val rsvpDao = db.rsvpDao()
    private val offlineTrailDao = db.offlineTrailDao()
    private val inboxDao = db.inboxDao()

    val trailsFlow: Flow<List<Trail>> = trailDao.getAllTrailsFlow().map { list ->
        list.map { it.toDomain() }
    }

    val currentUserFlow: Flow<User?> = userDao.getCurrentUserFlow().map { it?.toDomain() }

    val activeUserIdFlow: Flow<String?> = sessionDao.getActiveUserIdFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val inboxMessagesFlow: Flow<List<InboxMessage>> = currentUserFlow.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else inboxDao.getMessagesForUserFlow(user.id).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getTrailById(id: String): Trail? = withContext(Dispatchers.IO) {
        trailDao.getTrailById(id)?.toDomain()
    }

    suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)?.toDomain()
    }

    fun getUserByIdFlow(id: String): Flow<User?> {
        return userDao.getUserByIdFlow(id).map { it?.toDomain() }
    }

    // ---------------------------------------------------------
    // USER AUTHENTICATION & SESSION MANAGEMENT
    // ---------------------------------------------------------

    companion object {
        fun hashPassword(password: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest((password + "HillYeahSA2026").toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        avatarUrl: String? = null,
        city: String = "",
        province: String = "Western Cape",
        experienceLevel: String = "Intermediate",
        interests: List<String> = emptyList(),
        bio: String = ""
    ): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your full name."))
        }
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters long."))
        }

        val existingUser = userDao.getUserByEmail(trimmedEmail)
        if (existingUser != null) {
            return@withContext Result.failure(Exception("An account with this email address already exists. Please log in."))
        }

        val userId = "usr-" + UUID.randomUUID().toString().take(8)
        val defaultAvatar = avatarUrl?.takeIf { it.isNotBlank() }
            ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"

        val userEntity = UserEntity(
            id = userId,
            name = trimmedName,
            email = trimmedEmail,
            passwordHash = hashPassword(password),
            location = if (city.isNotBlank()) "$city, $province" else province,
            city = city,
            province = province,
            experienceLevel = experienceLevel,
            bio = bio.trim(),
            avatarUrl = defaultAvatar,
            interests = interests.joinToString(", "),
            createdAt = System.currentTimeMillis()
        )

        userDao.insertUser(userEntity)
        sessionDao.setActiveSession(ActiveSessionEntity(id = 1, userId = userId))

        Result.success(userEntity.toDomain())
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your email address."))
        }
        if (password.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your password."))
        }

        val userEntity = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(Exception("No account found for $trimmedEmail. Please check your spelling or register."))

        val inputHash = hashPassword(password)
        if (userEntity.passwordHash.isNotBlank() && userEntity.passwordHash != inputHash) {
            return@withContext Result.failure(Exception("Incorrect password. Please verify and try again."))
        }

        sessionDao.setActiveSession(ActiveSessionEntity(id = 1, userId = userEntity.id))
        Result.success(userEntity.toDomain())
    }

    suspend fun logout(): Unit = withContext(Dispatchers.IO) {
        sessionDao.clearActiveSession()
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        if (newPassword.length < 6) {
            return@withContext Result.failure(Exception("New password must be at least 6 characters."))
        }

        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(Exception("No account found with this email."))

        val newHash = hashPassword(newPassword)
        userDao.updatePassword(trimmedEmail, newHash)
        Result.success(Unit)
    }

    suspend fun updateCurrentUser(user: User) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserById(user.id)
        val passHash = existing?.passwordHash ?: ""
        userDao.updateUser(UserEntity.fromDomain(user, passHash))
    }

    suspend fun loginWithSocial(
        provider: String, // "Google", "Facebook"
        email: String,
        name: String,
        avatarUrl: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            sessionDao.setActiveSession(ActiveSessionEntity(id = 1, userId = existing.id))
            return@withContext Result.success(existing.toDomain())
        }

        val userId = "usr-" + UUID.randomUUID().toString().take(8)
        val defaultAvatar = avatarUrl?.takeIf { it.isNotBlank() }
            ?: if (provider.equals("Google", ignoreCase = true)) {
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80"
            } else {
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=400&q=80"
            }

        val newUser = UserEntity(
            id = userId,
            name = name.trim().ifBlank { "$provider Hiker" },
            email = trimmedEmail,
            passwordHash = "",
            location = "Cape Town, Western Cape",
            city = "Cape Town",
            province = "Western Cape",
            experienceLevel = "Intermediate",
            bio = "Connected via $provider. Passionate about exploring South African nature trails and community hikes.",
            avatarUrl = defaultAvatar,
            interests = "Social Group Hikes, Sunrise Hikes, Mindfulness & Meditation",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        sessionDao.setActiveSession(ActiveSessionEntity(id = 1, userId = userId))
        Result.success(newUser.toDomain())
    }

    suspend fun loginWithMagicLink(email: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext Result.failure(Exception("Please enter a valid email address for the magic sign-in link."))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            sessionDao.setActiveSession(ActiveSessionEntity(id = 1, userId = existing.id))
            return@withContext Result.success(existing.toDomain())
        }

        val userId = "usr-" + UUID.randomUUID().toString().take(8)
        val derivedName = trimmedEmail.substringBefore("@")
            .split(".", "_", "-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            .ifBlank { "Hiker" }

        val newUser = UserEntity(
            id = userId,
            name = derivedName,
            email = trimmedEmail,
            passwordHash = "",
            location = "Johannesburg, Gauteng",
            city = "Johannesburg",
            province = "Gauteng",
            experienceLevel = "Intermediate",
            bio = "Joined via Email Magic Link. Exploring Mzansi's scenic mountain reserves.",
            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=400&q=80",
            interests = "Social Group Hikes, Waterfall & River Walks, Fynbos & Flora",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        sessionDao.setActiveSession(ActiveSessionEntity(id = 1, userId = userId))
        Result.success(newUser.toDomain())
    }

    // ---------------------------------------------------------
    // HIKES & RSVPS
    // ---------------------------------------------------------

    fun getAllHikesWithDetailsFlow(): Flow<List<HikeWithDetails>> {
        return combine(
            hikeDao.getAllHikesFlow(),
            userDao.getCurrentUserFlow()
        ) { hikes, userEntity ->
            val user = userEntity?.toDomain()
            hikes.map { hikeEntity ->
                val hike = hikeEntity.toDomain()
                val activeRsvps = rsvpDao.getActiveRsvpsForHike(hike.id).map { it.toDomain() }
                val attendeesCount = activeRsvps.sumOf { it.guestsCount }
                val userRsvp = user?.let { u -> activeRsvps.find { it.userId == u.id } }
                val isHost = user?.id == hike.hostId
                val isFull = attendeesCount >= hike.capacity
                val spotsLeft = (hike.capacity - attendeesCount).coerceAtLeast(0)

                HikeWithDetails(
                    hike = hike,
                    attendeesCount = attendeesCount,
                    attendees = activeRsvps,
                    userRsvp = userRsvp,
                    isFull = isFull,
                    spotsLeft = spotsLeft,
                    isHost = isHost
                )
            }
        }
    }

    fun getHikeWithDetailsFlow(hikeId: String): Flow<HikeWithDetails?> {
        return combine(
            hikeDao.getHikeByIdFlow(hikeId),
            userDao.getCurrentUserFlow(),
            rsvpDao.getActiveRsvpsForHikeFlow(hikeId)
        ) { hikeEntity, userEntity, activeRsvpEntities ->
            if (hikeEntity == null) return@combine null

            val hike = hikeEntity.toDomain()
            val activeRsvps = activeRsvpEntities.map { it.toDomain() }
            val attendeesCount = activeRsvps.sumOf { it.guestsCount }
            val user = userEntity?.toDomain()
            val userRsvp = user?.let { u -> activeRsvps.find { it.userId == u.id } }
            val isHost = user?.id == hike.hostId
            val isFull = attendeesCount >= hike.capacity
            val spotsLeft = (hike.capacity - attendeesCount).coerceAtLeast(0)

            HikeWithDetails(
                hike = hike,
                attendeesCount = attendeesCount,
                attendees = activeRsvps,
                userRsvp = userRsvp,
                isFull = isFull,
                spotsLeft = spotsLeft,
                isHost = isHost
            )
        }
    }

    suspend fun createHike(
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
        description: String
    ): Result<Hike> = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUser() ?: return@withContext Result.failure(Exception("Please log in to host a group hike."))

        val hikeId = "hk-" + UUID.randomUUID().toString().take(8)
        val selectedTrail = trailId?.let { trailDao.getTrailById(it) }
        val imageUrl = selectedTrail?.imageUrl
            ?: "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1000&q=80"

        val cleanedTitle = title.trim()
        if (cleanedTitle.isBlank() || cleanedTitle.equals("the", ignoreCase = true) || cleanedTitle.length < 3) {
            return@withContext Result.failure(Exception("Please provide a proper, full hike title (e.g., 'Mindful Sunrise at Hennops')."))
        }

        val cleanedTrailName = if (customTrailName.isNotBlank() && !customTrailName.trim().equals("the", ignoreCase = true)) {
            customTrailName.trim()
        } else {
            selectedTrail?.name ?: "Hennops Krokodilberg Trail"
        }

        val newHike = HikeEntity(
            id = hikeId,
            hostId = user.id,
            hostName = user.name,
            hostAvatar = user.avatarUrl,
            title = cleanedTitle,
            trailId = trailId,
            trailName = cleanedTrailName,
            province = province,
            location = location.trim().ifBlank { selectedTrail?.location ?: "South Africa" },
            date = date,
            time = time,
            meetingPoint = meetingPoint.trim().ifBlank { selectedTrail?.meetingPoint ?: "Main Trailhead Car Park" },
            difficulty = difficulty,
            distanceKm = distanceKm,
            capacity = capacity.coerceAtLeast(2),
            description = description.trim(),
            status = "active",
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis()
        )

        hikeDao.insertHike(newHike)

        // Automatically enroll host with initial RSVP
        rsvpDao.insertOrUpdateRsvp(
            RsvpEntity(
                id = "rsvp-" + UUID.randomUUID().toString().take(8),
                hikeId = hikeId,
                userId = user.id,
                userName = user.name,
                userAvatar = user.avatarUrl,
                guestsCount = 1,
                status = "going",
                updatedAt = System.currentTimeMillis()
            )
        )

        Result.success(newHike.toDomain())
    }

    suspend fun updateHike(
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
        description: String
    ): Result<Hike> = withContext(Dispatchers.IO) {
        val hike = hikeDao.getHikeById(hikeId)
            ?: return@withContext Result.failure(Exception("Hike not found."))

        val currentUser = userDao.getCurrentUser()
        if (currentUser?.id != hike.hostId) {
            return@withContext Result.failure(Exception("Only the hike host can edit these details."))
        }

        val cleanedTitle = title.trim()
        if (cleanedTitle.isBlank() || cleanedTitle.equals("the", ignoreCase = true) || cleanedTitle.length < 3) {
            return@withContext Result.failure(Exception("Please provide a proper, full hike title (e.g., 'Mindful Sunrise at Hennops')."))
        }

        val cleanedTrail = if (trailName.isNotBlank() && !trailName.trim().equals("the", ignoreCase = true)) {
            trailName.trim()
        } else {
            hike.trailName.takeIf { !it.equals("the", ignoreCase = true) } ?: "Hennops Krokodilberg Trail"
        }

        val updated = hike.copy(
            title = cleanedTitle,
            trailName = cleanedTrail,
            location = location.trim().ifBlank { hike.location },
            province = province.ifBlank { hike.province },
            date = date.ifBlank { hike.date },
            time = time.ifBlank { hike.time },
            meetingPoint = meetingPoint.trim().ifBlank { hike.meetingPoint },
            difficulty = difficulty.ifBlank { hike.difficulty },
            distanceKm = if (distanceKm > 0) distanceKm else hike.distanceKm,
            capacity = capacity.coerceAtLeast(2),
            description = description.trim().ifBlank { hike.description }
        )

        hikeDao.updateHike(updated)
        Result.success(updated.toDomain())
    }

    suspend fun repairInvalidHikes() = withContext(Dispatchers.IO) {
        val allHikes = hikeDao.getAllHikes()
        for (hike in allHikes) {
            var needsUpdate = false
            var fixedTitle = hike.title.trim()
            var fixedTrailName = hike.trailName.trim()

            if (fixedTitle.isBlank() || fixedTitle.equals("the", ignoreCase = true) || fixedTitle.length < 3) {
                fixedTitle = if (fixedTrailName.isNotBlank() && !fixedTrailName.equals("the", ignoreCase = true)) {
                    "$fixedTrailName Group Trek"
                } else {
                    "Mindful Sunrise at Hennops"
                }
                needsUpdate = true
            }

            if (fixedTrailName.isBlank() || fixedTrailName.equals("the", ignoreCase = true)) {
                val matchedTrail = hike.trailId?.let { trailDao.getTrailById(it) }
                fixedTrailName = matchedTrail?.name ?: "Hennops Krokodilberg Trail"
                needsUpdate = true
            }

            if (needsUpdate) {
                hikeDao.updateHike(
                    hike.copy(
                        title = fixedTitle,
                        trailName = fixedTrailName
                    )
                )
            }
        }
    }

    suspend fun cancelHike(hikeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val hike = hikeDao.getHikeById(hikeId) ?: return@withContext Result.failure(Exception("Hike not found"))
        val currentUser = userDao.getCurrentUser()
        if (currentUser?.id != hike.hostId) {
            return@withContext Result.failure(Exception("Only the hike host can cancel this outing."))
        }

        hikeDao.cancelHike(hikeId)
        Result.success(Unit)
    }

    suspend fun submitRsvp(
        hikeId: String,
        status: String,
        guestsCount: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser()
            ?: return@withContext Result.failure(Exception("Account required. Please log in or register to RSVP."))

        val hike = hikeDao.getHikeById(hikeId)
            ?: return@withContext Result.failure(Exception("Hike not found."))

        if (hike.status == "cancelled") {
            return@withContext Result.failure(Exception("This hike has been cancelled by the host."))
        }

        val existingRsvps = rsvpDao.getActiveRsvpsForHike(hikeId)
        val otherAttendees = existingRsvps
            .filter { it.userId != currentUser.id }
            .sumOf { it.guestsCount }

        val requestedCount = guestsCount.coerceIn(1, 4)

        if (status == "going") {
            if (otherAttendees + requestedCount > hike.capacity) {
                val remaining = (hike.capacity - otherAttendees).coerceAtLeast(0)
                return@withContext Result.failure(
                    Exception("Cannot RSVP: Only $remaining spot${if (remaining == 1) "" else "s"} remaining on this hike.")
                )
            }

            val existingRsvp = rsvpDao.getRsvp(hikeId, currentUser.id)
            val rsvpToSave = RsvpEntity(
                id = existingRsvp?.id ?: ("rsvp-" + UUID.randomUUID().toString().take(8)),
                hikeId = hikeId,
                userId = currentUser.id,
                userName = currentUser.name,
                userAvatar = currentUser.avatarUrl,
                guestsCount = requestedCount,
                status = "going",
                updatedAt = System.currentTimeMillis()
            )
            rsvpDao.insertOrUpdateRsvp(rsvpToSave)
            Result.success("RSVP confirmed! See you on the trail.")
        } else {
            val existingRsvp = rsvpDao.getRsvp(hikeId, currentUser.id)
            if (existingRsvp != null) {
                rsvpDao.insertOrUpdateRsvp(
                    existingRsvp.copy(
                        status = "not_going",
                        guestsCount = 0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success("RSVP updated: Can't make it.")
        }
    }

    // ---------------------------------------------------------
    // SHARED HIKE INVITE LINK & INBOX FEEDBACK
    // ---------------------------------------------------------

    suspend fun respondToHikeInvite(
        hikeId: String,
        recipientHostId: String,
        action: String, // "accepted" or "declined"
        notes: String = "",
        guestName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val hike = hikeDao.getHikeById(hikeId)
            ?: return@withContext Result.failure(Exception("Hike invitation details not found."))

        val currentUser = userDao.getCurrentUser()
        val responderName = when {
            !guestName.isNullOrBlank() -> guestName.trim()
            currentUser != null -> currentUser.name
            else -> "Guest Hiker"
        }
        val responderId = currentUser?.id ?: "guest-" + UUID.randomUUID().toString().take(6)
        val responderAvatar = currentUser?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"

        // Record RSVP status against specific hikeId
        if (action == "accepted") {
            if (currentUser != null && guestName.isNullOrBlank()) {
                submitRsvp(hikeId, "going", 1)
            } else {
                // Public/unauthenticated guest RSVP entry recorded directly in the database
                val guestRsvp = RsvpEntity(
                    id = "rsvp-" + UUID.randomUUID().toString().take(8),
                    hikeId = hikeId,
                    userId = responderId,
                    userName = responderName,
                    userAvatar = responderAvatar,
                    guestsCount = 1,
                    status = "going",
                    updatedAt = System.currentTimeMillis()
                )
                rsvpDao.insertOrUpdateRsvp(guestRsvp)
            }
        } else if (action == "declined" && currentUser != null) {
            val existing = rsvpDao.getRsvp(hikeId, currentUser.id)
            if (existing != null) {
                rsvpDao.insertOrUpdateRsvp(existing.copy(status = "not_going", updatedAt = System.currentTimeMillis()))
            }
        }

        // Target the actual host of the hike if recipientHostId is blank
        val targetHostId = if (recipientHostId.isNotBlank()) recipientHostId else hike.hostId

        // Trigger in-app inbox notification for host: "[Name/Guest] has accepted your invitation for the [Hike Title]!"
        val actionVerb = if (action == "accepted") "accepted" else "declined"
        val notificationNote = if (notes.isNotBlank()) {
            "$responderName has $actionVerb your invitation for \"${hike.title}\": \"${notes.trim()}\""
        } else {
            "$responderName has $actionVerb your invitation for \"${hike.title}\"!"
        }

        val messageEntity = InboxMessageEntity(
            id = "inbox-" + UUID.randomUUID().toString().take(8),
            recipientUserId = targetHostId,
            senderUserId = responderId,
            senderName = responderName,
            senderAvatar = responderAvatar,
            hikeId = hike.id,
            hikeTitle = hike.title,
            action = action,
            notes = notificationNote,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        inboxDao.insertMessage(messageEntity)

        val confirmationText = if (action == "accepted") {
            "You've successfully accepted this invitation! The host has been notified."
        } else {
            "You've declined this invitation. The host has been notified."
        }

        Result.success(confirmationText)
    }

    suspend fun markInboxMessageAsRead(messageId: String) = withContext(Dispatchers.IO) {
        inboxDao.markAsRead(messageId)
    }

    suspend fun deleteInboxMessage(messageId: String) = withContext(Dispatchers.IO) {
        inboxDao.deleteMessage(messageId)
    }

    fun getDashboardHikesFlow(): Flow<DashboardHikes> {
        return combine(
            getAllHikesWithDetailsFlow(),
            currentUserFlow
        ) { allHikes, user ->
            if (user == null) {
                return@combine DashboardHikes(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
            }
            filterUserHikes(allHikes, user.id)
        }
    }

    fun getMemberHikesFlow(userId: String): Flow<DashboardHikes> {
        return getAllHikesWithDetailsFlow().map { allHikes ->
            filterUserHikes(allHikes, userId)
        }
    }

    private fun filterUserHikes(allHikes: List<HikeWithDetails>, userId: String): DashboardHikes {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val upcoming = mutableListOf<HikeWithDetails>()
        val hosting = mutableListOf<HikeWithDetails>()
        val joined = mutableListOf<HikeWithDetails>()
        val completed = mutableListOf<HikeWithDetails>()
        val cancelled = mutableListOf<HikeWithDetails>()

        for (item in allHikes) {
            val isHost = item.hike.hostId == userId
            val isGoing = item.attendees.any { it.userId == userId && it.status == "going" }

            if (!isHost && !isGoing) continue

            if (item.hike.status == "cancelled") {
                cancelled.add(item)
                continue
            }

            if (item.hike.date < todayDate) {
                completed.add(item)
            } else {
                upcoming.add(item)
                if (isHost) {
                    hosting.add(item)
                }
                if (isGoing && !isHost) {
                    joined.add(item)
                }
            }
        }

        return DashboardHikes(
            upcoming = upcoming.sortedBy { it.hike.date },
            hosting = hosting.sortedBy { it.hike.date },
            joined = joined.sortedBy { it.hike.date },
            completed = completed.sortedByDescending { it.hike.date },
            cancelled = cancelled.sortedByDescending { it.hike.createdAt }
        )
    }

    // ---------------------------------------------------------
    // OFFLINE ROOM CACHING MECHANISM FOR TRAILS & MAP DATA
    // ---------------------------------------------------------

    fun getOfflineTrailMapFlow(trailId: String): Flow<OfflineTrailMap?> {
        return offlineTrailDao.getOfflineTrailFlow(trailId).map { it?.toDomain() }
    }

    fun isTrailCachedFlow(trailId: String): Flow<Boolean> {
        return offlineTrailDao.isTrailCachedFlow(trailId)
    }

    fun getAllOfflineTrailsFlow(): Flow<List<OfflineTrailMap>> {
        return offlineTrailDao.getAllOfflineTrailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun cacheTrailForOffline(trail: Trail): Result<OfflineTrailMap> = withContext(Dispatchers.IO) {
        try {
            val waypoints = generateWaypointsForTrail(trail)
            val elevationProfile = generateElevationProfileForTrail(trail)
            val cacheSize = 1_850_000L + (trail.distanceKm * 125_000).toLong()

            val offlineMap = OfflineTrailMap(
                trailId = trail.id,
                trailName = trail.name,
                downloadedTimestamp = System.currentTimeMillis(),
                cacheSizeBytes = cacheSize,
                offlineNotes = "Offline Cache for ${trail.name}: Topographic GPS contour markers, water points, emergency waypoints, and elevation profile stored locally in Room. Ready for zero-reception hiking.",
                waypoints = waypoints,
                elevationProfile = elevationProfile
            )

            offlineTrailDao.insertOrUpdateOfflineTrail(OfflineTrailMapEntity.fromDomain(offlineMap))
            Result.success(offlineMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeOfflineTrail(trailId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            offlineTrailDao.deleteOfflineTrail(trailId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateWaypointsForTrail(trail: Trail): List<TrailWaypoint> {
        val baseLat = when (trail.province) {
            "Western Cape" -> -33.9351
            "Gauteng" -> -25.7479
            "KwaZulu-Natal" -> -28.7511
            "Mpumalanga" -> -24.5765
            "Eastern Cape" -> -34.0321
            "Free State" -> -28.5085
            "North West" -> -25.7198
            "Limpopo" -> -23.8967
            else -> -28.6012
        }
        val baseLng = when (trail.province) {
            "Western Cape" -> 18.3962
            "Gauteng" -> 28.2293
            "KwaZulu-Natal" -> 28.9142
            "Mpumalanga" -> 30.8241
            "Eastern Cape" -> 25.5654
            "Free State" -> 28.6183
            "North West" -> 27.2401
            "Limpopo" -> 30.0132
            else -> 20.3541
        }

        val dist = trail.distanceKm
        val elev = trail.elevationM

        return listOf(
            TrailWaypoint(
                name = "${trail.name} Trailhead",
                latitude = baseLat,
                longitude = baseLng,
                elevationM = (elev * 0.35).toInt().coerceAtLeast(50),
                kmMark = 0.0,
                description = "${trail.meetingPoint}. Information board and mobile signal active.",
                type = "trailhead"
            ),
            TrailWaypoint(
                name = "First Ascent Viewpoint",
                latitude = baseLat + 0.0035,
                longitude = baseLng + 0.0028,
                elevationM = (elev * 0.55).toInt().coerceAtLeast(100),
                kmMark = (dist * 0.28).coerceAtLeast(0.5),
                description = "Panoramic view over ${trail.location}. Good photo spot.",
                type = "viewpoint"
            ),
            TrailWaypoint(
                name = "Midway Rest & Spring Water Point",
                latitude = baseLat + 0.0065,
                longitude = baseLng + 0.0051,
                elevationM = (elev * 0.72).toInt().coerceAtLeast(180),
                kmMark = (dist * 0.55).coerceAtLeast(1.5),
                description = "Shaded natural clearing. Natural water stream when flowing in season.",
                type = "water"
            ),
            TrailWaypoint(
                name = "Contour Ridge & Scramble Beacon",
                latitude = baseLat + 0.0090,
                longitude = baseLng + 0.0075,
                elevationM = (elev * 0.88).toInt().coerceAtLeast(250),
                kmMark = (dist * 0.80).coerceAtLeast(2.0),
                description = "Rocky section with natural stone steps. Watch your footing.",
                type = "hazard"
            ),
            TrailWaypoint(
                name = "${trail.name} Summit Trig Beacon",
                latitude = baseLat + 0.0115,
                longitude = baseLng + 0.0098,
                elevationM = elev,
                kmMark = dist,
                description = "Highest point on route. 360-degree panoramic vista across the province.",
                type = "summit"
            )
        )
    }

    private fun generateElevationProfileForTrail(trail: Trail): List<ElevationPoint> {
        val steps = 6
        val list = mutableListOf<ElevationPoint>()
        val baseElev = (trail.elevationM * 0.35).toInt().coerceAtLeast(50)
        val peakElev = trail.elevationM
        for (i in 0..steps) {
            val fraction = i.toDouble() / steps
            val d = Math.round(trail.distanceKm * fraction * 10) / 10.0
            val e = (baseElev + (peakElev - baseElev) * Math.sin(fraction * Math.PI / 2)).toInt()
            list.add(ElevationPoint(d, e))
        }
        return list
    }
}
