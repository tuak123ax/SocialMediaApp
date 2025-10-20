package com.minhtu.firesocialmedia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // --- ADD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(users: List<UserEntity>)

    // --- READ (one-shot) ---
    @Query("SELECT * FROM UserFriends WHERE uid = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM UserFriends ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<UserEntity>

    // --- READ (reactive) ---
    @Query("SELECT * FROM UserFriends WHERE uid = :id")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM UserFriends ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<UserEntity>>

    // --- DELETE ---
    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM UserFriends WHERE uid = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM UserFriends WHERE uid IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM UserFriends")
    suspend fun clear()
}
