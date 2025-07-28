package com.minhtu.firesocialmedia.utils

import com.minhtu.firesocialmedia.data.model.news.CommentInstance
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.data.model.notification.fromMap


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

            val likedComments = (this["likedComments"] as? Map<*, *>)?.mapNotNull { entry ->
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
                friends = friends,
                likedComments = likedComments
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
                video = this["video"] as? String ?: "",
                isVisible = this["isVisible"] as? Boolean ?: true,
                likeCount = (this@toNewsInstance["likeCount"] as? Long)?.toInt() ?: 0,
                commentCount = (this@toNewsInstance["commentCount"] as? Long)?.toInt() ?: 0,
                timePosted = this@toNewsInstance["timePosted"] as? Long ?: 0
            )
        }

        fun Map<String, Any?>.toCommentInstance(): CommentInstance {
            return CommentInstance(
                id = this["id"] as? String ?: "",
                posterId = this["posterId"] as? String ?: "",
                posterName = this["posterName"] as? String ?: "",
                avatar = this["avatar"] as? String ?: "",
                message = this["message"] as? String ?: "",
                image = this["image"] as? String ?: "",
                video = this["video"] as? String ?: "",
                likeCount = (this@toCommentInstance["likeCount"] as? Long)?.toInt() ?: 0,
                commentCount = (this@toCommentInstance["commentCount"] as? Long)?.toInt() ?: 0,
                timePosted = this@toCommentInstance["timePosted"] as? Long ?: 0L,
                listReplies = HashMap(
                    (this["listReplies"] as? Map<*, *>)?.mapNotNull { (key, value) ->
                        val k = key as? String
                        val v = (value as? Map<String, Any?>)?.toCommentInstance()
                        if (k != null && v != null) k to v else null
                    }?.toMap() ?: emptyMap()
                )
            )
        }
    }
}