package com.minhtu.firesocialmedia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "News",
    indices = [Index(value = ["timePosted", "id"])]
)
data class NewsEntity(
    @PrimaryKey var id: String = "",
    var posterId : String = "",
    var posterName: String = "",
    var avatar: String = "",
    var message: String = "",
    var image: String = "",
    var video: String = "",
    var isVisible: Boolean = true,
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var timePosted: Long = 0
)