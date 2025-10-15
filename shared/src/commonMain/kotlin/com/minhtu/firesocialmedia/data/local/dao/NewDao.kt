package com.minhtu.firesocialmedia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minhtu.firesocialmedia.data.local.entity.NewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewDao {
    // --- ADD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(new: NewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(news: List<NewEntity>)

    // --- READ (one-shot) ---
    @Query("SELECT * FROM News WHERE id = :id")
    suspend fun getById(id: String): NewEntity?

    // --- READ (reactive) ---
    @Query("SELECT * FROM News WHERE id = :id")
    fun observeById(id: String): Flow<NewEntity?>

    // --- DELETE ---
    @Delete
    suspend fun delete(user: NewEntity)

    @Query("DELETE FROM News WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM News WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM News")
    suspend fun clear()
}