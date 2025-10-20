package com.minhtu.firesocialmedia.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "UserFriends",
    indices = [
        Index("uid", unique = true)
    ]
)
data class UserEntity(
    var email: String = "",
    var image: String = "",
    var name: String = "",
    var status: String = "",
    var token: String = "",
    @PrimaryKey var uid: String = ""
//    var likedPosts: HashMap<String,Int> = HashMap(),
//    var friendRequests : ArrayList<String> = ArrayList(),
//    var notifications : ArrayList<NotificationDTO> = ArrayList(),
//    var friends : ArrayList<String> = ArrayList(),
//    var likedComments : HashMap<String,Int> = HashMap()
)