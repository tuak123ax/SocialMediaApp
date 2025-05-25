package com.minhtu.firesocialmedia.instance

import kotlinx.serialization.Serializable

@Serializable
data class UserInstance(var email: String = "", var image: String = "", var name: String = "",
                        var status: String = "", var token: String = "", var uid: String = "",
                        var likedPosts: HashMap<String,Boolean> = HashMap(),
                        var friendRequests : ArrayList<String> = ArrayList(),
                        var notifications : ArrayList<NotificationInstance> = ArrayList(),
                        var friends : ArrayList<String> = ArrayList()
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
    fun addNotification(notification: NotificationInstance) {
        notifications.add(notification)
    }
    fun removeNotification(notification: NotificationInstance) {
        notifications.remove(notification)
    }
}

fun UserInstance.toMap(): Map<String, Any?> = mapOf(
    "email" to email,
    "image" to image,
    "name" to name,
    "status" to status,
    "token" to token,
    "uid" to uid,
    "likedPosts" to likedPosts, // Firebase supports maps of simple types
    "friendRequests" to friendRequests,
    "friends" to friends,
    "notifications" to notifications.map { it.toMap() }
)

