package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.InboxMessageEntity
import com.example.data.local.OfflineTrailMapEntity
import com.example.data.local.TrailSeedData
import com.example.data.local.UserEntity
import com.example.data.model.ElevationPoint
import com.example.data.model.Hike
import com.example.data.model.InboxMessage
import com.example.data.model.OfflineTrailMap
import com.example.data.model.Rsvp
import com.example.data.model.Trail
import com.example.data.model.TrailWaypoint
import com.example.data.model.User
import com.example.data.repository.HillYeahRepository
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HillYeah", appName)
    }

    @Test
    fun `trail model validation across provinces`() {
        val trail = Trail(
            id = "tr-wc-01",
            name = "Lion's Head Summit Trail",
            province = "Western Cape",
            location = "Cape Town",
            difficulty = "Moderate",
            distanceKm = 5.3,
            durationHrs = "2.5 - 3",
            elevationM = 669,
            description = "Iconic trail",
            meetingPoint = "Parking Lot",
            imageUrl = "",
            gearTips = "Water and shoes"
        )
        assertEquals("Western Cape", trail.province)
        assertEquals(5.3, trail.distanceKm, 0.01)
    }

    @Test
    fun `capacity enforcement calculation`() {
        val hike = Hike(
            id = "hk-01",
            hostId = "usr-01",
            hostName = "Thabo",
            hostAvatar = "",
            title = "Lion's Head Sunrise",
            trailId = "tr-wc-01",
            trailName = "Lion's Head",
            province = "Western Cape",
            location = "Cape Town",
            date = "2026-10-25",
            time = "06:00",
            meetingPoint = "Parking",
            difficulty = "Moderate",
            distanceKm = 5.3,
            capacity = 10,
            description = "Sunrise hike",
            imageUrl = ""
        )

        val rsvps = listOf(
            Rsvp("r1", hike.id, "usr-01", "Thabo", "", 1, "going"),
            Rsvp("r2", hike.id, "usr-02", "Lerato", "", 3, "going")
        )
        val occupied = rsvps.sumOf { it.guestsCount }
        assertEquals(4, occupied)

        val spotsRemaining = hike.capacity - occupied
        assertEquals(6, spotsRemaining)
        assertTrue(spotsRemaining >= 2)
    }

    @Test
    fun `password hashing security and consistency`() {
        val rawPassword = "Password123!"
        val hash1 = HillYeahRepository.hashPassword(rawPassword)
        val hash2 = HillYeahRepository.hashPassword(rawPassword)
        val hashDiff = HillYeahRepository.hashPassword("DifferentPass")

        assertEquals(hash1, hash2)
        assertNotEquals(rawPassword, hash1)
        assertNotEquals(hash1, hashDiff)
        assertEquals(64, hash1.length) // SHA-256 hex string length
    }

    @Test
    fun `user entity domain round trip conversion with province and interests`() {
        val user = User(
            id = "usr-test-01",
            name = "Sindi Khumalo",
            email = "sindi@hillyeah.co.za",
            location = "Durban, KwaZulu-Natal",
            city = "Durban",
            province = "KwaZulu-Natal",
            experienceLevel = "Experienced",
            bio = "Lover of Drakensberg escarpments and coastal dune walks.",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
            interests = listOf("Sunrise Hikes", "Peak Scrambling", "Fynbos & Flora")
        )

        val entity = UserEntity.fromDomain(user, "testHash123")
        assertEquals("usr-test-01", entity.id)
        assertEquals("sindi@hillyeah.co.za", entity.email)
        assertEquals("testHash123", entity.passwordHash)
        assertEquals("KwaZulu-Natal", entity.province)
        assertTrue(entity.interests.contains("Sunrise Hikes"))

        val domain = entity.toDomain()
        assertEquals(user.name, domain.name)
        assertEquals(3, domain.interests.size)
        assertTrue(domain.interests.contains("Peak Scrambling"))
    }

    @Test
    fun `offline trail map entity serialization and caching`() {
        val waypoints = listOf(
            TrailWaypoint(
                name = "Krokodilberg Trailhead",
                latitude = -25.7479,
                longitude = 28.2293,
                elevationM = 320,
                kmMark = 0.0,
                description = "Hennops river suspension bridge",
                type = "trailhead"
            ),
            TrailWaypoint(
                name = "Pruimpie se Grot & Water Point",
                latitude = -25.7430,
                longitude = 28.2340,
                elevationM = 480,
                kmMark = 4.5,
                description = "Historical cave and water stream",
                type = "water"
            )
        )

        val elevationProfile = listOf(
            ElevationPoint(0.0, 320),
            ElevationPoint(4.5, 480),
            ElevationPoint(9.5, 640)
        )

        val offlineMap = OfflineTrailMap(
            trailId = "tr-gp-01",
            trailName = "Hennops Krokodilberg Trail",
            downloadedTimestamp = 1700000000000L,
            cacheSizeBytes = 2_500_000L,
            offlineNotes = "Offline Cache verified",
            waypoints = waypoints,
            elevationProfile = elevationProfile
        )

        // Convert to Room Entity
        val entity = OfflineTrailMapEntity.fromDomain(offlineMap)
        assertEquals("tr-gp-01", entity.trailId)
        assertTrue(entity.waypointsJson.contains("Krokodilberg Trailhead"))

        // Reconvert to Domain
        val domain = entity.toDomain()
        assertEquals(2, domain.waypoints.size)
        assertEquals("Pruimpie se Grot & Water Point", domain.waypoints[1].name)
        assertEquals(3, domain.elevationProfile.size)
        assertEquals(640, domain.elevationProfile[2].elevationM)
    }

    @Test
    fun `all 9 provinces have exactly 20 trails seeded totaling 180 trails`() {
        val trails = TrailSeedData.getAllTrails()
        assertEquals(180, trails.size)

        val byProvince = trails.groupBy { it.province }
        assertEquals(9, byProvince.size)

        val expectedProvinces = listOf(
            "Western Cape", "Gauteng", "KwaZulu-Natal", "Mpumalanga",
            "Eastern Cape", "Free State", "Limpopo", "North West", "Northern Cape"
        )
        for (province in expectedProvinces) {
            val provinceTrails = byProvince[province]
            assertNotNull("Province $province should have trails", provinceTrails)
            assertEquals("Province $province must have exactly 20 trails", 20, provinceTrails!!.size)
        }
    }

    @Test
    fun `waterfall filter finds trails with waterfalls across multiple provinces`() {
        val trails = TrailSeedData.getAllTrails()
        val waterfallTrails = trails.filter { it.hasWaterfall }
        assertTrue("There should be abundant waterfall trails", waterfallTrails.size >= 40)

        // Verify known waterfall trails
        val tugela = waterfallTrails.find { it.name.contains("Tugela", ignoreCase = true) }
        assertNotNull("Tugela Gorge should have waterfall flag", tugela)
        assertTrue(tugela!!.hasWaterfall)

        val macMac = waterfallTrails.find { it.name.contains("Mac Mac", ignoreCase = true) }
        assertNotNull("Mac Mac Falls should have waterfall flag", macMac)
        assertTrue(macMac!!.hasWaterfall)
    }

    @Test
    fun `inbox message entity domain round trip conversion for invite response`() {
        val message = InboxMessage(
            id = "msg-inv-99",
            recipientUserId = "usr-01",
            senderUserId = "usr-02",
            senderName = "Lerato Khumalo",
            senderAvatar = "https://example.com/avatar.jpg",
            hikeId = "hk-01",
            hikeTitle = "Lion's Head Sunrise Ascent",
            action = "accepted",
            notes = "Bringing hot rooibos tea and camera! See you at 05:45.",
            timestamp = 1700000000000L,
            isRead = false
        )

        val entity = InboxMessageEntity.fromDomain(message)
        assertEquals("msg-inv-99", entity.id)
        assertEquals("accepted", entity.action)
        assertEquals("Bringing hot rooibos tea and camera! See you at 05:45.", entity.notes)

        val domain = entity.toDomain()
        assertEquals(message.senderName, domain.senderName)
        assertEquals(message.hikeTitle, domain.hikeTitle)
        assertEquals("accepted", domain.action)
        assertEquals(false, domain.isRead)
    }

    @Test
    fun `all 180 trails are accurately located with directions and website links`() {
        val trails = TrailSeedData.getAllTrails()
        assertEquals(180, trails.size)

        for (trail in trails) {
            // Geographic coordinates must be within South Africa
            // Latitude in SA ranges roughly between -35.0 and -22.0
            assertTrue(
                "Trail ${trail.name} latitude (${trail.latitude}) should be in South Africa",
                trail.latitude in -35.5..-22.0
            )
            // Longitude in SA ranges roughly between 16.0 and 33.5
            assertTrue(
                "Trail ${trail.name} longitude (${trail.longitude}) should be in South Africa",
                trail.longitude in 16.0..33.5
            )

            // Directions URL must be a valid Google Maps directions link
            assertTrue(
                "Trail ${trail.name} directionsUrl must be a Google Maps directions URI",
                trail.directionsUrl.startsWith("https://www.google.com/maps/dir/")
            )

            // Website URL must be a valid https link (SANParks, CapeNature, Ezemvelo, MTPA, etc.)
            assertTrue(
                "Trail ${trail.name} websiteUrl must be a valid https URL",
                trail.websiteUrl.startsWith("https://")
            )
        }

        // Domain conversion test
        val firstTrail = trails.first().toDomain()
        assertTrue(firstTrail.directionsUrl.isNotEmpty())
        assertTrue(firstTrail.websiteUrl.isNotEmpty())
        assertEquals(trails.first().latitude, firstTrail.latitude, 0.0001)
        assertEquals(trails.first().longitude, firstTrail.longitude, 0.0001)
    }

    @Test
    fun `public unauthenticated invite response creates rsvp and notifies host inbox`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getInstance(context)
        com.example.data.local.AppDatabase.seedDatabase(db)
        db.sessionDao().clearActiveSession()
        val repository = HillYeahRepository(db)

        val hikes = repository.getAllHikesWithDetailsFlow().first()
        val targetHike = hikes.first().hike

        val result = repository.respondToHikeInvite(
            hikeId = targetHike.id,
            recipientHostId = targetHike.hostId,
            action = "accepted",
            notes = "Super excited! Bringing snacks.",
            guestName = "Sindi Khumalo"
        )

        assertTrue(result.isSuccess)
        assertEquals(
            "You've successfully accepted this invitation! The host has been notified.",
            result.getOrNull()
        )

        // Verify notification delivered to host inbox
        val hostInbox = db.inboxDao().getMessagesForUser(targetHike.hostId)
        val notifyMsg = hostInbox.find { it.hikeId == targetHike.id && it.senderName == "Sindi Khumalo" }
        assertNotNull(notifyMsg)
        assertEquals("accepted", notifyMsg?.action)
        assertTrue(notifyMsg?.notes?.contains("Sindi Khumalo has accepted your invitation") == true)

        // Verify guest RSVP created
        val rsvps = db.rsvpDao().getActiveRsvpsForHike(targetHike.id)
        val guestRsvp = rsvps.find { it.userName == "Sindi Khumalo" }
        assertNotNull(guestRsvp)
        assertEquals("going", guestRsvp?.status)
    }

    @Test
    fun `public unauthenticated invite decline notifies host inbox without error`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getInstance(context)
        com.example.data.local.AppDatabase.seedDatabase(db)
        db.sessionDao().clearActiveSession()
        val repository = HillYeahRepository(db)

        val hikes = repository.getAllHikesWithDetailsFlow().first()
        val targetHike = hikes.first().hike

        val result = repository.respondToHikeInvite(
            hikeId = targetHike.id,
            recipientHostId = targetHike.hostId,
            action = "declined",
            notes = "Can't make it this weekend, sorry!",
            guestName = "Guest Hiker"
        )

        assertTrue(result.isSuccess)
        assertEquals(
            "You've declined this invitation. The host has been notified.",
            result.getOrNull()
        )

        val hostInbox = db.inboxDao().getMessagesForUser(targetHike.hostId)
        val notifyMsg = hostInbox.find { it.hikeId == targetHike.id && it.action == "declined" }
        assertNotNull(notifyMsg)
        assertTrue(notifyMsg?.notes?.contains("Guest Hiker has declined your invitation") == true)
    }

    @Test
    fun `standardized hikId query parameter extraction routes to invite response`() {
        val testUrl = "https://ais-pre-jqceebarzt5bk5rzvpfovv-267217831212.europe-west2.run.app/?hikId=hk-01&hostId=usr-01"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(testUrl))
        val (extractedId, extractedHost) = MainActivity.extractHikeParams(intent)

        assertEquals("hk-01", extractedId)
        assertEquals("usr-01", extractedHost)
    }

    @Test
    fun `hash router link extraction prevents server 404 and extracts parameters cleanly`() {
        val hashUrl = "https://ais-pre-jqceebarzt5bk5rzvpfovv-267217831212.europe-west2.run.app/#/?hikId=hk-01&hostId=usr-01"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(hashUrl))
        val (extractedId, extractedHost) = MainActivity.extractHikeParams(intent)

        assertEquals("hk-01", extractedId)
        assertEquals("usr-01", extractedHost)
    }

    @Test
    fun `incoming hikeld typo parameter fallback extraction`() {
        val typoUrl = "https://ais-pre-jqceebarzt5bk5rzvpfovv-267217831212.europe-west2.run.app/#/?hikeld=hk-02&hostId=usr-02"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(typoUrl))
        val (extractedId, extractedHost) = MainActivity.extractHikeParams(intent)

        assertEquals("hk-02", extractedId)
        assertEquals("usr-02", extractedHost)
    }
}
