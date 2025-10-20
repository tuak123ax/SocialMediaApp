package com.minhtu.firesocialmedia.data.remote.dto.user

import com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.remote.dto.notification.toMap
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(var email: String = "", var image: String = "", var name: String = "",
                   var status: String = "", var token: String = "", var uid: String = "",
                   var likedPosts: HashMap<String,Int> = HashMap(),
                   var friendRequests : ArrayList<String> = ArrayList(),
                   var notifications : ArrayList<NotificationDTO> = ArrayList(),
                   var friends : ArrayList<String> = ArrayList(),
                   var likedComments : HashMap<String,Int> = HashMap()
)
{
    fun addFriend(friend: String){
        friends.add(friend)
    }
    fun removeFriend(friend: String){
        friends.remove(friend)
    }
    fun addFriendRequest(friend: String){
        friendRequests.add(friend)
    }
    fun removeFriendRequest(friend: String){
        friendRequests.remove(friend)
    }
    fun updateImage(image: String){
        this.image = image
    }
    fun addNotification(notification: NotificationDTO) {
        notifications.add(notification)
    }
    fun removeNotification(notification: NotificationDTO) {
        notifications.remove(notification)
    }
}

fun UserDTO.toMap(): Map<String, Any?> = mapOf(
    "email" to email,
    "image" to image,
    "name" to name,
    "status" to status,
    "token" to token,
    "uid" to uid,
    "likedPosts" to likedPosts,
    "likedComments" to likedComments,
    "friendRequests" to friendRequests,
    "friends" to friends,
    "notifications" to notifications.map { it.toMap() }
)

