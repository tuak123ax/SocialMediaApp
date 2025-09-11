package com.minhtu.firesocialmedia.utils

import com.minhtu.firesocialmedia.data.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.data.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.dto.notification.fromMap
import com.minhtu.firesocialmedia.data.dto.user.UserDTO


class IosUtils {
    companion object{
        fun Map<*, *>.toUserDTO(): UserDTO {
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
                (item as? Map<*, *>)?.let { NotificationDTO.fromMap(it as Map<String, Any?>) }
            }?.let { ArrayList(it) } ?: arrayListOf()

            val friends = (this["friends"] as? List<*>)?.mapNotNull { it as? String }
                ?.let { ArrayList(it) } ?: ArrayList()

            return UserDTO(
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

        fun Map<String, Any?>.toNewsDTO(): NewsDTO {
            return NewsDTO(
                id = this["id"] as? String ?: "",
                posterId = this["posterId"] as? String ?: "",
                posterName = this["posterName"] as? String ?: "",
                avatar = this["avatar"] as? String ?: "",
                message = this["message"] as? String ?: "",
                image = this["image"] as? String ?: "",
                video = this["video"] as? String ?: "",
                isVisible = this["isVisible"] as? Boolean ?: true,
                likeCount = (this@toNewsDTO["likeCount"] as? Long)?.toInt() ?: 0,
                commentCount = (this@toNewsDTO["commentCount"] as? Long)?.toInt() ?: 0,
                timePosted = this@toNewsDTO["timePosted"] as? Long ?: 0
            )
        }

        fun Map<String, Any?>.toCommentDTO(): CommentDTO {
            return CommentDTO(
                id = this["id"] as? String ?: "",
                posterId = this["posterId"] as? String ?: "",
                posterName = this["posterName"] as? String ?: "",
                avatar = this["avatar"] as? String ?: "",
                message = this["message"] as? String ?: "",
                image = this["image"] as? String ?: "",
                video = this["video"] as? String ?: "",
                likeCount = (this@toCommentDTO["likeCount"] as? Long)?.toInt() ?: 0,
                commentCount = (this@toCommentDTO["commentCount"] as? Long)?.toInt() ?: 0,
                timePosted = this@toCommentDTO["timePosted"] as? Long ?: 0L,
                listReplies = HashMap(
                    (this["listReplies"] as? Map<*, *>)?.mapNotNull { (key, value) ->
                        val k = key as? String
                        val v = (value as? Map<String, Any?>)?.toCommentDTO()
                        if (k != null && v != null) k to v else null
                    }?.toMap() ?: emptyMap()
                )
            )
        }
    }
}