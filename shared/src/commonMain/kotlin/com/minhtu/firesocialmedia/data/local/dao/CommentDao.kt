package com.minhtu.firesocialmedia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minhtu.firesocialmedia.data.local.entity.CommentEntity

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveComment(commentEntity : CommentEntity)

    @Query("DELETE FROM Comments")
    suspend fun clear()

    @Query("SELECT EXISTS(SELECT 1 FROM Comments LIMIT 1)")
    suspend fun hasAnyComments(): Boolean

    @Query("SELECT * FROM Comments")
    suspend fun getAllComments(): List<CommentEntity>
}