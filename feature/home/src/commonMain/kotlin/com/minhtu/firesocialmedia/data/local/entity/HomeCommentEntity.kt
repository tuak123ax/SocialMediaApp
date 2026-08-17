package com.minhtu.firesocialmedia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "HomeComments",
    indices = [Index(value = ["id"])]
)
data class HomeCommentEntity(
    @PrimaryKey
    var id: String = "",
    var posterId : String = "",
    var posterName: String = "",
    var avatar: String = "",
    var message: String = "",
    var video: String = "",
    var image: String = "",
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var timePosted: Long = 0,
    var selectedNewId : String = ""
)
