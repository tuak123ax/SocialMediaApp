package com.minhtu.firesocialmedia.utils

import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.instance.fromMap


class IosUtils {
    companion object{
        fun Map<*, *>.toUserInstance(): UserInstance {
            val likedPosts = (this["likedPosts"] as? Map<*, *>)?.mapNotNull { entry ->
                val key = entry.key as? String
                val value = when (val rawValue = entry.value) {
                    is Number -> rawValue.toInt()
                    else -> null
                }
                if (key != null && value != null) key to value else null
            }?.toMap()?.let { HashMap(it) } ?: HashMap()

            val friendRequests = (this["friendRequests"] as? List<*>)?.mapNotNull { it as? String }
                ?.let { ArrayList(it) } ?: ArrayList()

            val notifications = (this["notifications"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { NotificationInstance.fromMap(it as Map<String, Any?>) }
            }?.let { ArrayList(it) } ?: arrayListOf()

            val friends = (this["friends"] as? List<*>)?.mapNotNull { it as? String }
                ?.let { ArrayList(it) } ?: ArrayList()

            return UserInstance(
                email = this["email"] as? String ?: "",
                image = this["image"] as? String ?: "",
                name = this["name"] as? String ?: "",
                status = this["status"] as? String ?: "",
                token = this["token"] as? String ?: "",
                uid = this["uid"] as? String ?: "",
                likedPosts = likedPosts,
                friendRequests = friendRequests,
                notifications = notifications,
                friends = friends
            )
        }

        fun Map<String, Any?>.toNewsInstance(): NewsInstance {
            return NewsInstance(
                id = this["id"] as? String ?: "",
                posterId = this["posterId"] as? String ?: "",
                posterName = this["posterName"] as? String ?: "",
                avatar = this["avatar"] as? String ?: "",
                message = this["message"] as? String ?: "",
                image = this["image"] as? String ?: "",
                isVisible = this["isVisible"] as? Boolean ?: true
            ).apply {
                likeCount = (this@toNewsInstance["likeCount"] as? Long)?.toInt() ?: 0
                commentCount = (this@toNewsInstance["commentCount"] as? Long)?.toInt() ?: 0
                timePosted = this@toNewsInstance["timePosted"] as? Long ?: 0
            }
        }

        fun Map<String, Any?>.toCommentInstance(): CommentInstance {
            return CommentInstance(
                id = this["id"] as? String ?: "",
                posterId = this["posterId"] as? String ?: "",
                posterName = this["posterName"] as? String ?: "",
                avatar = this["avatar"] as? String ?: "",
                message = this["message"] as? String ?: "",
                image = this["image"] as? String ?: ""
            ).apply {
                likeCount = (this@toCommentInstance["likeCount"] as? Long)?.toInt() ?: 0
                commentCount = (this@toCommentInstance["commentCount"] as? Long)?.toInt() ?: 0
                timePosted = this@toCommentInstance["timePosted"] as? Long ?: 0L
            }
        }
    }
}