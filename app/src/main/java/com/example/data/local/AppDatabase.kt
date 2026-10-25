package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrailEntity::class,
        UserEntity::class,
        HikeEntity::class,
        RsvpEntity::class,
        OfflineTrailMapEntity::class,
        ActiveSessionEntity::class,
        InboxMessageEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
    abstract fun userDao(): UserDao
    abstract fun hikeDao(): HikeDao
    abstract fun rsvpDao(): RsvpDao
    abstract fun offlineTrailDao(): OfflineTrailDao
    abstract fun sessionDao(): SessionDao
    abstract fun inboxDao(): InboxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hillyeah_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDatabase(database)
                                }
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDatabase(db: AppDatabase) {
            val passHash = com.example.data.repository.HillYeahRepository.hashPassword("Password123!")

            val user1 = UserEntity(
                id = "usr-thabo-01",
                name = "Thabo Mokoena",
                email = "thabo.mokoena@hillyeah.co.za",
                passwordHash = passHash,
                location = "Cape Town, Western Cape",
                city = "Cape Town",
                province = "Western Cape",
                experienceLevel = "Experienced",
                bio = "Avid Highveld & Cape mountain wanderer. Passionate about mindful hiking, fynbos conservation, and building outdoor community bonds.",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                interests = "Sunrise Hikes, Fynbos & Flora, Peak Scrambling, Photography",
                createdAt = System.currentTimeMillis() - 86400000L * 45
            )
            db.userDao().insertUser(user1)

            val user2 = UserEntity(
                id = "usr-lerato-02",
                name = "Lerato Ndlovu",
                email = "lerato.ndlovu@hillyeah.co.za",
                passwordHash = passHash,
                location = "Johannesburg, Gauteng",
                city = "Johannesburg",
                province = "Gauteng",
                experienceLevel = "Intermediate",
                bio = "Weekend trail enthusiast exploring the Magaliesberg and Drakensberg Escarpment. Passionate about women's hiking groups and wellness in nature.",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                interests = "Social Group Hikes, Trail Running, Bird Watching, Multi-Day Backpacking",
                createdAt = System.currentTimeMillis() - 86400000L * 30
            )
            db.userDao().insertUser(user2)

            // Seed 20 curated trails in each of the 9 provinces (180 trails total)
            db.trailDao().insertTrails(TrailSeedData.getAllTrails())

            // Seed initial community hikes
            val hike1 = HikeEntity(
                id = "hk-lionshead-sunrise",
                hostId = user1.id,
                hostName = user1.name,
                hostAvatar = user1.avatarUrl,
                title = "Mindful Sunrise Ascent up Lion's Head",
                trailId = "tr-wc-01",
                trailName = "Lion's Head Summit Trail",
                province = "Western Cape",
                location = "Cape Town (Table Mountain NP)",
                date = "2026-10-25",
                time = "05:45",
                meetingPoint = "Lion's Head Parking Lot, Signal Hill Road (look for the green HillYeah flag)",
                difficulty = "Moderate",
                distanceKm = 5.3,
                capacity = 12,
                description = "Join us for an uplifting sunrise trek! We'll take an easy, mindful pace around the spiral path, enjoy silent meditation at the summit while the sun crests the Hottentots Holland Mountains, and share rooibos tea.",
                status = "active",
                imageUrl = "https://images.unsplash.com/photo-1580618672591-eb180b1a973f?auto=format&fit=crop&w=1000&q=80",
                createdAt = System.currentTimeMillis()
            )
            db.hikeDao().insertHike(hike1)

            db.rsvpDao().insertOrUpdateRsvp(
                RsvpEntity(
                    id = "rsvp-host-01",
                    hikeId = hike1.id,
                    userId = user1.id,
                    userName = user1.name,
                    userAvatar = user1.avatarUrl,
                    guestsCount = 1,
                    status = "going",
                    updatedAt = System.currentTimeMillis()
                )
            )
            db.rsvpDao().insertOrUpdateRsvp(
                RsvpEntity(
                    id = "rsvp-guest-02",
                    hikeId = hike1.id,
                    userId = "usr-lerato-02",
                    userName = "Lerato Ndlovu",
                    userAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                    guestsCount = 2,
                    status = "going",
                    updatedAt = System.currentTimeMillis()
                )
            )
            db.rsvpDao().insertOrUpdateRsvp(
                RsvpEntity(
                    id = "rsvp-guest-03",
                    hikeId = hike1.id,
                    userId = "usr-johan-03",
                    userName = "Johan van der Merwe",
                    userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                    guestsCount = 1,
                    status = "going",
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Seed Hike 2: Drakensberg Tugela Gorge
            val hike2 = HikeEntity(
                id = "hk-drakensberg-amphitheatre",
                hostId = "usr-sipho-04",
                hostName = "Sipho Dlamini",
                hostAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80",
                title = "Tugela Gorge & Drakensberg Amphitheatre Trek",
                trailId = "tr-kzn-01",
                trailName = "Tugela Gorge Trail",
                province = "KwaZulu-Natal",
                location = "Royal Natal National Park",
                date = "2026-11-01",
                time = "07:30",
                meetingPoint = "Thendele Camp Parking Trailhead by the wooden bridge",
                difficulty = "Moderate",
                distanceKm = 14.0,
                capacity = 10,
                description = "Experience the sheer power of the Northern Drakensberg. We will trek along the Tugela riverbed, explore the gorge boulders and river tunnel, with frequent breaks to absorb nature's healing peace.",
                status = "active",
                imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1000&q=80",
                createdAt = System.currentTimeMillis()
            )
            db.hikeDao().insertHike(hike2)

            db.rsvpDao().insertOrUpdateRsvp(
                RsvpEntity(
                    id = "rsvp-host-04",
                    hikeId = hike2.id,
                    userId = "usr-sipho-04",
                    userName = "Sipho Dlamini",
                    userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80",
                    guestsCount = 1,
                    status = "going",
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Seed initial invite feedback in Host Inbox
            val seedInbox1 = InboxMessageEntity(
                id = "inbox-seed-01",
                recipientUserId = user1.id,
                senderUserId = "usr-lerato-02",
                senderName = "Lerato Ndlovu",
                senderAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                hikeId = hike1.id,
                hikeTitle = hike1.title,
                action = "accepted",
                notes = "Accepted invite! Bringing homemade rusks and hot rooibos tea for the sunrise at the summit.",
                timestamp = System.currentTimeMillis() - 3600000L * 2,
                isRead = false
            )
            db.inboxDao().insertMessage(seedInbox1)

            val seedInbox2 = InboxMessageEntity(
                id = "inbox-seed-02",
                recipientUserId = user1.id,
                senderUserId = "usr-johan-03",
                senderName = "Johan van der Merwe",
                senderAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                hikeId = hike1.id,
                hikeTitle = hike1.title,
                action = "accepted",
                notes = "Accepted! I'll meet you guys at the Lion's Head car park at 05:30. Let's conquer the chains!",
                timestamp = System.currentTimeMillis() - 3600000L * 14,
                isRead = true
            )
            db.inboxDao().insertMessage(seedInbox2)

            // Seed initial offline cached map for Lion's Head Summit
            val lionHeadWaypoints = listOf(
                com.example.data.model.TrailWaypoint(
                    name = "Signal Hill Road Trailhead",
                    latitude = -33.9351,
                    longitude = 18.3962,
                    elevationM = 240,
                    kmMark = 0.0,
                    description = "Starting car park & TMNP information board",
                    type = "trailhead"
                ),
                com.example.data.model.TrailWaypoint(
                    name = "Lower Kramat & Resting Bench",
                    latitude = -33.9340,
                    longitude = 18.3935,
                    elevationM = 360,
                    kmMark = 1.2,
                    description = "Historic shrine and shaded stone bench overlooking Table Bay",
                    type = "rest"
                ),
                com.example.data.model.TrailWaypoint(
                    name = "Contour Path & Emergency Marker",
                    latitude = -33.9332,
                    longitude = 18.3895,
                    elevationM = 450,
                    kmMark = 2.3,
                    description = "South contour junction. Clear cellular emergency reception spot.",
                    type = "water"
                ),
                com.example.data.model.TrailWaypoint(
                    name = "Staple Ladder & Chain Scramble",
                    latitude = -33.9348,
                    longitude = 18.3888,
                    elevationM = 550,
                    kmMark = 3.4,
                    description = "Rocky scramble with fixed chains; easy bypass path branches left",
                    type = "hazard"
                ),
                com.example.data.model.TrailWaypoint(
                    name = "Lion's Head Summit Beacon",
                    latitude = -33.9350,
                    longitude = 18.3890,
                    elevationM = 669,
                    kmMark = 5.3,
                    description = "Panoramic 360-degree peak with concrete trig beacon",
                    type = "summit"
                )
            )

            val lionHeadElevation = listOf(
                com.example.data.model.ElevationPoint(0.0, 240),
                com.example.data.model.ElevationPoint(1.0, 320),
                com.example.data.model.ElevationPoint(2.0, 420),
                com.example.data.model.ElevationPoint(3.0, 510),
                com.example.data.model.ElevationPoint(4.0, 590),
                com.example.data.model.ElevationPoint(5.3, 669)
            )

            val lionHeadOffline = OfflineTrailMapEntity(
                trailId = "tr-wc-01",
                trailName = "Lion's Head Summit Trail",
                downloadedTimestamp = System.currentTimeMillis(),
                cacheSizeBytes = 2_450_000L,
                offlineNotes = "Offline Topographic Cache: Full GPS coordinates, offline waypoints, and contour profile saved on device. No cellular data required on trail.",
                waypointsJson = OfflineTrailMapEntity.serializeWaypoints(lionHeadWaypoints),
                elevationProfileJson = OfflineTrailMapEntity.serializeElevationProfile(lionHeadElevation)
            )
            db.offlineTrailDao().insertOrUpdateOfflineTrail(lionHeadOffline)
        }
    }
}
