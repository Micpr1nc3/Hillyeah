package com.example.data.model

data class Trail(
    val id: String,
    val name: String,
    val province: String,
    val location: String,
    val difficulty: String, // Easy, Moderate, Challenging
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
)

data class TrailWaypoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevationM: Int,
    val kmMark: Double,
    val description: String,
    val type: String // "trailhead", "water", "viewpoint", "summit", "hazard", "rest"
)

data class ElevationPoint(
    val distanceKm: Double,
    val elevationM: Int
)

data class OfflineTrailMap(
    val trailId: String,
    val trailName: String,
    val downloadedTimestamp: Long,
    val cacheSizeBytes: Long,
    val offlineNotes: String,
    val waypoints: List<TrailWaypoint>,
    val elevationProfile: List<ElevationPoint>
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val location: String = "",
    val city: String = "",
    val province: String = "Western Cape",
    val experienceLevel: String = "Intermediate", // Beginner, Intermediate, Experienced
    val bio: String = "",
    val avatarUrl: String = "",
    val interests: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Hike(
    val id: String,
    val hostId: String,
    val hostName: String,
    val hostAvatar: String,
    val title: String,
    val trailId: String?,
    val trailName: String,
    val province: String,
    val location: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val meetingPoint: String,
    val difficulty: String,
    val distanceKm: Double,
    val capacity: Int,
    val description: String,
    val status: String = "active", // active, cancelled
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Rsvp(
    val id: String,
    val hikeId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val guestsCount: Int = 1,
    val status: String = "going", // going, not_going
    val updatedAt: Long = System.currentTimeMillis()
)

data class HikeWithDetails(
    val hike: Hike,
    val attendeesCount: Int,
    val attendees: List<Rsvp>,
    val userRsvp: Rsvp?,
    val isFull: Boolean,
    val spotsLeft: Int,
    val isHost: Boolean
)

data class InboxMessage(
    val id: String,
    val recipientUserId: String, // The host who sent or owns the hike
    val senderUserId: String,    // The receiver who accepted or declined
    val senderName: String,
    val senderAvatar: String,
    val hikeId: String,
    val hikeTitle: String,
    val action: String,          // "accepted" or "declined"
    val notes: String = "",      // Any personal notes left by receiver
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class HikingQuote(
    val quote: String,
    val author: String,
    val context: String = "South African Trails"
)

data class WeatherReport(
    val location: String,
    val province: String,
    val temperatureC: Int,
    val condition: String,
    val highC: Int,
    val lowC: Int,
    val windSpeedKmh: Int,
    val humidityPercent: Int,
    val uvIndex: String,
    val trailSuitability: String
)

object HillYeahConstants {
    const val SharedAppBaseUrl = "https://ais-pre-jqceebarzt5bk5rzvpfovv-267217831212.europe-west2.run.app"

    val SouthAfricanProvinces = listOf(
        "Western Cape",
        "Gauteng",
        "KwaZulu-Natal",
        "Mpumalanga",
        "Eastern Cape",
        "Free State",
        "Limpopo",
        "North West",
        "Northern Cape"
    )

    val ExperienceLevels = listOf(
        "Beginner",
        "Intermediate",
        "Experienced"
    )

    val HikingInterests = listOf(
        "Sunrise Hikes",
        "Sunset & Full Moon",
        "Peak Scrambling",
        "Fynbos & Flora",
        "Photography",
        "Social Group Hikes",
        "Trail Running",
        "Multi-Day Backpacking",
        "Dog-Friendly Trails",
        "Bird Watching",
        "Mindfulness & Meditation",
        "Waterfall & River Walks"
    )

    val PresetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80"
    )

    val HikingQuotes = listOf(
        HikingQuote(
            quote = "In every walk with nature one receives far more than he seeks.",
            author = "John Muir",
            context = "Wilderness Reflection"
        ),
        HikingQuote(
            quote = "After climbing a great hill, one only finds that there are many more hills to climb.",
            author = "Nelson Mandela",
            context = "Long Walk to Freedom"
        ),
        HikingQuote(
            quote = "Heal the world, one hike at a time.",
            author = "HillYeah Community Tagline",
            context = "Ubuntu in Nature"
        ),
        HikingQuote(
            quote = "The mountains are calling and I must go.",
            author = "John Muir",
            context = "Mountain Spirit"
        ),
        HikingQuote(
            quote = "Those who contemplate the beauty of the earth find reserves of strength that will endure as long as life lasts.",
            author = "Rachel Carson",
            context = "Earth Solace"
        ),
        HikingQuote(
            quote = "It is not the mountain we conquer, but ourselves.",
            author = "Sir Edmund Hillary",
            context = "Summit Mindset"
        ),
        HikingQuote(
            quote = "To walk in nature is to witness a thousand miracles.",
            author = "Mary Davis",
            context = "Mindful Trekking"
        ),
        HikingQuote(
            quote = "Between every two pines is a doorway to a new world.",
            author = "John Muir",
            context = "Forest Peace"
        ),
        HikingQuote(
            quote = "Look deep into nature, and then you will understand everything better.",
            author = "Albert Einstein",
            context = "Nature's Wisdom"
        ),
        HikingQuote(
            quote = "Wilderness is not a luxury but a necessity of the human spirit.",
            author = "Edward Abbey",
            context = "Conservation"
        )
    )
}
