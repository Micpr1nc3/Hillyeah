package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails ORDER BY name ASC")
    fun getAllTrailsFlow(): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trails")
    suspend fun getAllTrails(): List<TrailEntity>

    @Query("SELECT * FROM trails WHERE id = :id LIMIT 1")
    suspend fun getTrailById(id: String): TrailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrails(trails: List<TrailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trail: TrailEntity)
}

@Dao
interface OfflineTrailDao {
    @Query("SELECT * FROM offline_trail_maps WHERE trailId = :trailId LIMIT 1")
    fun getOfflineTrailFlow(trailId: String): Flow<OfflineTrailMapEntity?>

    @Query("SELECT * FROM offline_trail_maps WHERE trailId = :trailId LIMIT 1")
    suspend fun getOfflineTrail(trailId: String): OfflineTrailMapEntity?

    @Query("SELECT * FROM offline_trail_maps ORDER BY downloadedTimestamp DESC")
    fun getAllOfflineTrailsFlow(): Flow<List<OfflineTrailMapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOfflineTrail(map: OfflineTrailMapEntity)

    @Query("DELETE FROM offline_trail_maps WHERE trailId = :trailId")
    suspend fun deleteOfflineTrail(trailId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM offline_trail_maps WHERE trailId = :trailId)")
    fun isTrailCachedFlow(trailId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM offline_trail_maps WHERE trailId = :trailId)")
    suspend fun isTrailCached(trailId: String): Boolean
}

@Dao
interface UserDao {
    @Query("SELECT u.* FROM users u INNER JOIN active_session s ON u.id = s.userId WHERE s.id = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT u.* FROM users u INNER JOIN active_session s ON u.id = s.userId WHERE s.id = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE LOWER(email) = LOWER(:email)")
    suspend fun updatePassword(email: String, newPasswordHash: String): Int
}

@Dao
interface SessionDao {
    @Query("SELECT userId FROM active_session WHERE id = 1 LIMIT 1")
    fun getActiveUserIdFlow(): Flow<String?>

    @Query("SELECT userId FROM active_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveUserId(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActiveSession(session: ActiveSessionEntity)

    @Query("UPDATE active_session SET userId = NULL WHERE id = 1")
    suspend fun clearActiveSession()
}

@Dao
interface HikeDao {
    @Query("SELECT * FROM hikes ORDER BY date ASC, time ASC")
    fun getAllHikesFlow(): Flow<List<HikeEntity>>

    @Query("SELECT * FROM hikes")
    suspend fun getAllHikes(): List<HikeEntity>

    @Query("SELECT * FROM hikes WHERE id = :id LIMIT 1")
    fun getHikeByIdFlow(id: String): Flow<HikeEntity?>

    @Query("SELECT * FROM hikes WHERE id = :id LIMIT 1")
    suspend fun getHikeById(id: String): HikeEntity?

    @Query("SELECT * FROM hikes WHERE hostId = :hostId ORDER BY date DESC")
    fun getHikesByHostFlow(hostId: String): Flow<List<HikeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: HikeEntity)

    @Update
    suspend fun updateHike(hike: HikeEntity)

    @Query("UPDATE hikes SET status = 'cancelled' WHERE id = :id")
    suspend fun cancelHike(id: String)
}

@Dao
interface RsvpDao {
    @Query("SELECT * FROM rsvps WHERE hikeId = :hikeId AND status = 'going'")
    fun getActiveRsvpsForHikeFlow(hikeId: String): Flow<List<RsvpEntity>>

    @Query("SELECT * FROM rsvps WHERE hikeId = :hikeId AND status = 'going'")
    suspend fun getActiveRsvpsForHike(hikeId: String): List<RsvpEntity>

    @Query("SELECT * FROM rsvps WHERE userId = :userId")
    fun getRsvpsForUserFlow(userId: String): Flow<List<RsvpEntity>>

    @Query("SELECT * FROM rsvps WHERE hikeId = :hikeId AND userId = :userId LIMIT 1")
    suspend fun getRsvp(hikeId: String, userId: String): RsvpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRsvp(rsvp: RsvpEntity)
}

@Dao
interface InboxDao {
    @Query("SELECT * FROM inbox_messages WHERE recipientUserId = :userId ORDER BY timestamp DESC")
    fun getMessagesForUserFlow(userId: String): Flow<List<InboxMessageEntity>>

    @Query("SELECT * FROM inbox_messages WHERE recipientUserId = :userId ORDER BY timestamp DESC")
    suspend fun getMessagesForUser(userId: String): List<InboxMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: InboxMessageEntity)

    @Query("UPDATE inbox_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM inbox_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)
}
