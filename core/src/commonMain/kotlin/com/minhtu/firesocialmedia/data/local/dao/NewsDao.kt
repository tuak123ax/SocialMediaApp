package com.minhtu.firesocialmedia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    // --- ADD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(new: NewsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(news: List<NewsEntity>)

    // --- READ (one-shot) ---
    @Query("SELECT * FROM News WHERE id = :id")
    suspend fun getById(id: String): NewsEntity?

    // --- READ (reactive) ---
    @Query("SELECT * FROM News WHERE id = :id")
    fun observeById(id: String): Flow<NewsEntity?>

    // --- DELETE ---
    @Delete
    suspend fun delete(new: NewsEntity)

    @Query("DELETE FROM News WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM News WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM News")
    suspend fun clear()

    @Query("DELETE FROM News WHERE id = :id")
    suspend fun deleteDraftPost(id : String)
    @Query("DELETE FROM News WHERE isNewPost = 1")
    suspend fun deleteAllDraftPosts()

    // Page 1 (no cursor)
    @Query("""
    SELECT * FROM News
    ORDER BY timePosted DESC, id DESC
    LIMIT :limit
  """)
    suspend fun firstPage(limit: Int): List<NewsEntity>

    // Next pages (cursor by timePosted + id)
    @Query("""
    SELECT * FROM News
    WHERE
      (:lastTimePosted IS NULL) OR
      (timePosted < :lastTimePosted) OR
      (timePosted = :lastTimePosted AND :lastKey IS NOT NULL AND id < :lastKey)
    ORDER BY timePosted DESC, id DESC
    LIMIT :limit
  """)
    suspend fun pageAfter(
        limit: Int,
        lastTimePosted: Long?,
        lastKey: String?
    ): List<NewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAllLikedPosts(likedPosts: List<LikedPostEntity>)
    @Query("SELECT * FROM likedPosts")
    suspend fun getAllLikedPosts(): List<LikedPostEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM likedPosts LIMIT 1)")
    suspend fun hasAnyLikedPosts(): Boolean

    @Query("DELETE FROM likedPosts")
    suspend fun clearLikedPosts()
    @Query("SELECT * FROM News WHERE isNewPost = 1")
    suspend fun loadNewsPostedWhenOffline(): List<NewsEntity>


}