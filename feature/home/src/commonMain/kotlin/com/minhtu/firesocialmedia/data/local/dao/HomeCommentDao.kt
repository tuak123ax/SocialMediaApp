package com.minhtu.firesocialmedia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minhtu.firesocialmedia.data.local.entity.HomeCommentEntity

@Dao
interface HomeCommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveComment(commentEntity : HomeCommentEntity)

    @Query("DELETE FROM HomeComments")
    suspend fun clear()

    @Query("SELECT EXISTS(SELECT 1 FROM HomeComments LIMIT 1)")
    suspend fun hasAnyComments(): Boolean

    @Query("SELECT * FROM HomeComments")
    suspend fun getAllComments(): List<HomeCommentEntity>
}
