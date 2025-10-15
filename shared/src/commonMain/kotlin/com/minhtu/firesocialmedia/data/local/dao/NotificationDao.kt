package com.minhtu.firesocialmedia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    // --- ADD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(notifications: List<NotificationEntity>)

    // --- READ (one-shot) ---
    @Query("SELECT * FROM Notifications WHERE id = :id")
    suspend fun getById(id: String): NotificationEntity?

    // --- READ (reactive) ---
    @Query("SELECT * FROM Notifications WHERE id = :id")
    fun observeById(id: String): Flow<NotificationEntity?>

    // --- DELETE ---
    @Delete
    suspend fun delete(user: NotificationEntity)

    @Query("DELETE FROM Notifications WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM Notifications WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM Notifications")
    suspend fun clear()
}