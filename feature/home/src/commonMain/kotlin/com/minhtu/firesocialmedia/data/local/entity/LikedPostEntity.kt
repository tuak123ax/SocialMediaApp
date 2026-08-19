package com.minhtu.firesocialmedia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "likedPosts",
    indices = [Index(value = ["id"])]
)
data class LikedPostEntity(
    @PrimaryKey
    val id : String = "",
    val isLiked : Int = 0
)