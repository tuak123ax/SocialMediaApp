package com.minhtu.firesocialmedia.friend.data.remote.dto.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserDTO(
    var email: String = "", var image: String = "", var name: String = "",
    var status: String = "", var phone: String = "", var token: String = "", var uid: String = "",
    var background: String = "",
    var likedPosts: HashMap<String, Int> = HashMap(),
    var friendRequests: ArrayList<String> = ArrayList(),
    var friends: ArrayList<String> = ArrayList(),
    var likedComments: HashMap<String, Int> = HashMap(),
    @Transient
    var groups: HashMap<String, Any?> = HashMap(),
    var lastTimeChangePassword: Long = 0,
    var twoFAEnabled: Boolean = false,
    var lastTimeReadPrivacy: Long = 0,
    var lastTimeAcknowledgedLoginHistory: Long = 0
) {
    fun addFriend(friend: String) {
        friends.add(friend)
    }

    fun removeFriend(friend: String) {
        friends.remove(friend)
    }

    fun addFriendRequest(friend: String) {
        friendRequests.add(friend)
    }

    fun removeFriendRequest(friend: String) {
        friendRequests.remove(friend)
    }

    fun updateImage(image: String) {
        this.image = image
    }

}

fun UserDTO.toMap(): Map<String, Any?> = mapOf(
    "email" to email,
    "image" to image,
    "name" to name,
    "status" to status,
    "token" to token,
    "phone" to phone,
    "uid" to uid,
    "background" to background,
    "likedPosts" to likedPosts,
    "likedComments" to likedComments,
    "friendRequests" to friendRequests,
    "friends" to friends,
    "groups" to groups,
    "lastTimeChangePassword" to lastTimeChangePassword,
    "twoFAEnabled" to twoFAEnabled,
    "lastTimeReadPrivacy" to lastTimeReadPrivacy,
    "lastTimeAcknowledgedLoginHistory" to lastTimeAcknowledgedLoginHistory
)
