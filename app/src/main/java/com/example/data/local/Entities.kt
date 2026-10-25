package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ElevationPoint
import com.example.data.model.Hike
import com.example.data.model.InboxMessage
import com.example.data.model.OfflineTrailMap
import com.example.data.model.Rsvp
import com.example.data.model.Trail
import com.example.data.model.TrailWaypoint
import com.example.data.model.User
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "trails")
data class TrailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val province: String,
    val location: String,
    val difficulty: String,
    val distanceKm: Double,
    val durationHrs: String,
    val elevationM: Int,
    val description: String,
    val meetingPoint: String,
    val imageUrl: String,
    val gearTips: String,
    val hasWaterfall: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val directionsUrl: String = "",
    val websiteUrl: String = ""
) {
    fun toDomain() = Trail(
        id = id,
        name = name,
        province = province,
        location = location,
        difficulty = difficulty,
        distanceKm = distanceKm,
        durationHrs = durationHrs,
        elevationM = elevationM,
        description = description,
        meetingPoint = meetingPoint,
        imageUrl = imageUrl,
        gearTips = gearTips,
        hasWaterfall = hasWaterfall,
        latitude = latitude,
        longitude = longitude,
        directionsUrl = directionsUrl,
        websiteUrl = websiteUrl
    )

    companion object {
        fun fromDomain(trail: Trail) = TrailEntity(
            id = trail.id,
            name = trail.name,
            province = trail.province,
            location = trail.location,
            difficulty = trail.difficulty,
            distanceKm = trail.distanceKm,
            durationHrs = trail.durationHrs,
            elevationM = trail.elevationM,
            description = trail.description,
            meetingPoint = trail.meetingPoint,
            imageUrl = trail.imageUrl,
            gearTips = trail.gearTips,
            hasWaterfall = trail.hasWaterfall,
            latitude = trail.latitude,
            longitude = trail.longitude,
            directionsUrl = trail.directionsUrl,
            websiteUrl = trail.websiteUrl
        )
    }
}

@Entity(tableName = "offline_trail_maps")
data class OfflineTrailMapEntity(
    @PrimaryKey val trailId: String,
    val trailName: String,
    val downloadedTimestamp: Long,
    val cacheSizeBytes: Long,
    val offlineNotes: String,
    val waypointsJson: String,
    val elevationProfileJson: String
) {
    fun toDomain(): OfflineTrailMap {
        val waypoints = parseWaypoints(waypointsJson)
        val profile = parseElevationProfile(elevationProfileJson)
        return OfflineTrailMap(
            trailId = trailId,
            trailName = trailName,
            downloadedTimestamp = downloadedTimestamp,
            cacheSizeBytes = cacheSizeBytes,
            offlineNotes = offlineNotes,
            waypoints = waypoints,
            elevationProfile = profile
        )
    }

    companion object {
        fun fromDomain(map: OfflineTrailMap): OfflineTrailMapEntity {
            return OfflineTrailMapEntity(
                trailId = map.trailId,
                trailName = map.trailName,
                downloadedTimestamp = map.downloadedTimestamp,
                cacheSizeBytes = map.cacheSizeBytes,
                offlineNotes = map.offlineNotes,
                waypointsJson = serializeWaypoints(map.waypoints),
                elevationProfileJson = serializeElevationProfile(map.elevationProfile)
            )
        }

        fun serializeWaypoints(waypoints: List<TrailWaypoint>): String {
            val array = JSONArray()
            waypoints.forEach { w ->
                val obj = JSONObject().apply {
                    put("name", w.name)
                    put("latitude", w.latitude)
                    put("longitude", w.longitude)
                    put("elevationM", w.elevationM)
                    put("kmMark", w.kmMark)
                    put("description", w.description)
                    put("type", w.type)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun parseWaypoints(jsonStr: String): List<TrailWaypoint> {
            val list = mutableListOf<TrailWaypoint>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TrailWaypoint(
                            name = obj.optString("name", "Waypoint"),
                            latitude = obj.optDouble("latitude", 0.0),
                            longitude = obj.optDouble("longitude", 0.0),
                            elevationM = obj.optInt("elevationM", 0),
                            kmMark = obj.optDouble("kmMark", 0.0),
                            description = obj.optString("description", ""),
                            type = obj.optString("type", "waypoint")
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        fun serializeElevationProfile(profile: List<ElevationPoint>): String {
            val array = JSONArray()
            profile.forEach { p ->
                val obj = JSONObject().apply {
                    put("distanceKm", p.distanceKm)
                    put("elevationM", p.elevationM)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun parseElevationProfile(jsonStr: String): List<ElevationPoint> {
            val list = mutableListOf<ElevationPoint>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ElevationPoint(
                            distanceKm = obj.optDouble("distanceKm", 0.0),
                            elevationM = obj.optInt("elevationM", 0)
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String = "",
    val location: String = "",
    val city: String = "",
    val province: String = "Western Cape",
    val experienceLevel: String = "Intermediate",
    val bio: String = "",
    val avatarUrl: String = "",
    val interests: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = User(
        id = id,
        name = name,
        email = email,
        location = if (location.isNotBlank()) location else (if (city.isNotBlank()) "$city, $province" else province),
        city = city,
        province = province,
        experienceLevel = experienceLevel,
        bio = bio,
        avatarUrl = avatarUrl,
        interests = if (interests.isBlank()) emptyList() else interests.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(user: User, passwordHash: String = "") = UserEntity(
            id = user.id,
            name = user.name,
            email = user.email.lowercase().trim(),
            passwordHash = passwordHash,
            location = user.location,
            city = user.city,
            province = user.province,
            experienceLevel = user.experienceLevel,
            bio = user.bio,
            avatarUrl = user.avatarUrl,
            interests = user.interests.joinToString(", "),
            createdAt = user.createdAt
        )
    }
}

@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: String?
)

@Entity(tableName = "hikes")
data class HikeEntity(
    @PrimaryKey val id: String,
    val hostId: String,
    val hostName: String,
    val hostAvatar: String,
    val title: String,
    val trailId: String?,
    val trailName: String,
    val province: String,
    val location: String,
    val date: String,
    val time: String,
    val meetingPoint: String,
    val difficulty: String,
    val distanceKm: Double,
    val capacity: Int,
    val description: String,
    val status: String,
    val imageUrl: String,
    val createdAt: Long
) {
    fun toDomain() = Hike(
        id = id,
        hostId = hostId,
        hostName = hostName,
        hostAvatar = hostAvatar,
        title = title,
        trailId = trailId,
        trailName = trailName,
        province = province,
        location = location,
        date = date,
        time = time,
        meetingPoint = meetingPoint,
        difficulty = difficulty,
        distanceKm = distanceKm,
        capacity = capacity,
        description = description,
        status = status,
        imageUrl = imageUrl,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(hike: Hike) = HikeEntity(
            id = hike.id,
            hostId = hike.hostId,
            hostName = hike.hostName,
            hostAvatar = hike.hostAvatar,
            title = hike.title,
            trailId = hike.trailId,
            trailName = hike.trailName,
            province = hike.province,
            location = hike.location,
            date = hike.date,
            time = hike.time,
            meetingPoint = hike.meetingPoint,
            difficulty = hike.difficulty,
            distanceKm = hike.distanceKm,
            capacity = hike.capacity,
            description = hike.description,
            status = hike.status,
            imageUrl = hike.imageUrl,
            createdAt = hike.createdAt
        )
    }
}

@Entity(tableName = "rsvps")
data class RsvpEntity(
    @PrimaryKey val id: String,
    val hikeId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val guestsCount: Int,
    val status: String,
    val updatedAt: Long
) {
    fun toDomain() = Rsvp(
        id = id,
        hikeId = hikeId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        guestsCount = guestsCount,
        status = status,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(rsvp: Rsvp) = RsvpEntity(
            id = rsvp.id,
            hikeId = rsvp.hikeId,
            userId = rsvp.userId,
            userName = rsvp.userName,
            userAvatar = rsvp.userAvatar,
            guestsCount = rsvp.guestsCount,
            status = rsvp.status,
            updatedAt = rsvp.updatedAt
        )
    }
}

@Entity(tableName = "inbox_messages")
data class InboxMessageEntity(
    @PrimaryKey val id: String,
    val recipientUserId: String,
    val senderUserId: String,
    val senderName: String,
    val senderAvatar: String,
    val hikeId: String,
    val hikeTitle: String,
    val action: String, // "accepted" or "declined"
    val notes: String,
    val timestamp: Long,
    val isRead: Boolean = false
) {
    fun toDomain() = InboxMessage(
        id = id,
        recipientUserId = recipientUserId,
        senderUserId = senderUserId,
        senderName = senderName,
        senderAvatar = senderAvatar,
        hikeId = hikeId,
        hikeTitle = hikeTitle,
        action = action,
        notes = notes,
        timestamp = timestamp,
        isRead = isRead
    )

    companion object {
        fun fromDomain(msg: InboxMessage) = InboxMessageEntity(
            id = msg.id,
            recipientUserId = msg.recipientUserId,
            senderUserId = msg.senderUserId,
            senderName = msg.senderName,
            senderAvatar = msg.senderAvatar,
            hikeId = msg.hikeId,
            hikeTitle = msg.hikeTitle,
            action = msg.action,
            notes = msg.notes,
            timestamp = msg.timestamp,
            isRead = msg.isRead
        )
    }
}
