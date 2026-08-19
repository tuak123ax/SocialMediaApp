package com.minhtu.firesocialmedia.comment.entity.user

import com.minhtu.firesocialmedia.comment.entity.notification.NotificationInstance

data class UserInstance(
    var email: String = "",
    var image: String = "",
    var name: String = "",
    var status: String = "",
    var phone: String = "",
    var token: String = "",
    var uid: String = "",
    var background: String = "",
    var likedPosts: HashMap<String, Int> = HashMap(),
    var friendRequests: ArrayList<String> = ArrayList(),
    var notifications: ArrayList<NotificationInstance> = ArrayList(),
    var friends: ArrayList<String> = ArrayList(),
    var likedComments: HashMap<String, Int> = HashMap(),
    var groups: HashSet<String> = HashSet(),
    var lastTimeChangePassword: Long = 0,
    var twoFAEnabled: Boolean = false,
    var lastTimeReadPrivacy: Long = 0,
    var lastTimeAcknowledgedLoginHistory: Long = 0
) {
    fun addFriend(friend: String) { friends.add(friend) }
    fun removeFriend(friend: String) { friends.remove(friend) }
    fun addFriendRequest(friend: String) { friendRequests.add(friend) }
    fun removeFriendRequest(friend: String) { friendRequests.remove(friend) }
    fun updateImage(image: String) { this.image = image }
    fun addNotification(notification: NotificationInstance) { notifications.add(notification) }
    fun removeNotification(notification: NotificationInstance) { notifications.remove(notification) }
    fun isDefault(): Boolean = email.isEmpty() && image.isEmpty() && name.isEmpty() && status.isEmpty() && phone.isEmpty() && token.isEmpty() && uid.isEmpty()
}
