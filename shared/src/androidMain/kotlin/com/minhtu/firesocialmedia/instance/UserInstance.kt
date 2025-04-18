package com.minhtu.firesocialmedia.instance

import com.minhtu.firesocialmedia.home.navigationscreen.notification.Notification
import org.json.JSONArray
import java.io.Serializable

data class UserInstance(var email: String = "", var image: String = "", var name: String = "",
                        var status: String = "", var token: String = "", var uid: String = "",
                        var likedPosts: HashMap<String,Boolean> = HashMap(),
                        var friendRequests : ArrayList<String> = ArrayList(),
                        var notifications : ArrayList<NotificationInstance> = ArrayList(),
                        var friends : ArrayList<String> = ArrayList()
):Serializable
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
