package com.minhtu.firesocialmedia.core.domain.entity.user

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.toMap

data class UserInstance(var email: String = "", var image: String = "", var name: String = "",
                        var status: String = "", var phone: String = "", var token: String = "", var uid: String = "",
                        var background: String = "",
                        var likedPosts: HashMap<String,Int> = HashMap(),
                        var friendRequests : ArrayList<String> = ArrayList(),
                        var notifications : ArrayList<NotificationInstance> = ArrayList(),
                        var friends : ArrayList<String> = ArrayList(),
                        var likedComments : HashMap<String,Int> = HashMap(),
                        var groups: HashMap<String, GroupInstance> = HashMap(),
                        var lastTimeChangePassword: Long = 0,
                        var twoFAEnabled : Boolean = false,
                        var lastTimeReadPrivacy: Long = 0,
                        var lastTimeAcknowledgedLoginHistory: Long = 0
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
    fun isDefault(): Boolean {
        return email.isEmpty() && image.isEmpty() && name.isEmpty() && status.isEmpty() && phone.isEmpty() && token.isEmpty() && uid.isEmpty()
    }
}

fun UserInstance.toMap(): Map<String, Any?> = mapOf(
    "email" to email,
    "image" to image,
    "name" to name,
    "status" to status,
    "phone" to phone,
    "token" to token,
    "uid" to uid,
    "background" to background,
    "likedPosts" to likedPosts,
    "likedComments" to likedComments,
    "friendRequests" to friendRequests,
    "friends" to friends,
    "notifications" to notifications.map { it.toMap() },
    "groups" to groups,
    "lastTimeChangePassword" to lastTimeChangePassword,
    "twoFAEnabled" to twoFAEnabled,
    "lastTimeReadPrivacy" to lastTimeReadPrivacy,
    "lastTimeAcknowledgedLoginHistory" to lastTimeAcknowledgedLoginHistory
)

