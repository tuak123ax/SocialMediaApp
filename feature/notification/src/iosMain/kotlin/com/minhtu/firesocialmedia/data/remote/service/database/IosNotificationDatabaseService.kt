package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.constants.notification.DataConstant
import com.minhtu.firesocialmedia.notification.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private fun Map<String, Any?>.toNotificationNewsDTO(): NewsDTO {
    return NewsDTO(
        id = this["id"] as? String ?: "",
        posterId = this["posterId"] as? String ?: "",
        posterName = this["posterName"] as? String ?: "",
        avatar = this["avatar"] as? String ?: "",
        message = this["message"] as? String ?: "",
        image = this["image"] as? String ?: "",
        video = this["video"] as? String ?: "",
        isVisible = this["isVisible"] as? Boolean ?: true,
        likeCount = (this["likeCount"] as? Long)?.toInt() ?: 0,
        commentCount = (this["commentCount"] as? Long)?.toInt() ?: 0,
        timePosted = this["timePosted"] as? Long ?: 0,
        localPath = this["localPath"] as? String ?: "",
        shareContentId = this["shareContentId"] as? String ?: "",
        decentralizationType = this["decentralizationType"] as? String ?: "",
        type = this["type"] as? String,
        pollId = this["pollId"] as? String
    )
}

private fun Map<*, *>.toNotificationUserDTO(): UserDTO {
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
        friends = friends,
        likedComments = likedComments
    )
}

class IosNotificationDatabaseService : NotificationDatabaseService {
    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? {
        val raw = suspendCancellableCoroutine<NewsDTO?> { continuation ->
            val databaseReference = FIRDatabase.database().reference()
                .child(newsPath)
                .child(newId)
            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    val rawValue = (snapshot?.takeIf { it.exists() }?.value as? Map<*, *>)
                    if (rawValue != null) {
                        try {
                            val value = rawValue.entries.associate {
                                (it.key as? String) to it.value
                            }.filterKeys { it != null } as Map<String, Any?>
                            if (continuation.isActive) continuation.resume(value.toNotificationNewsDTO()) {}
                        } catch (_: Exception) {
                            if (continuation.isActive) continuation.resume(null) {}
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(null) {}
                    }
                }
            ) { _ -> if (continuation.isActive) continuation.resume(null) {} }
        } ?: return null

        raw.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(raw.avatar)
        raw.image = SupabaseStorageHelper.resolveMediaUrlAsync(raw.image)
        raw.video = SupabaseStorageHelper.resolveMediaUrlAsync(raw.video)
        return raw
    }

    override suspend fun getUser(userId: String): UserDTO? {
        val rawUser = suspendCancellableCoroutine<UserDTO?> { continuation ->
            val databaseReference = FIRDatabase.database().reference()
                .child(DataConstant.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *>
                        if (value != null) {
                            try {
                                val user = value.toNotificationUserDTO()
                                if (continuation.isActive) continuation.resume(user) {}
                            } catch (_: Exception) {
                                if (continuation.isActive) continuation.resume(null) {}
                            }
                        } else {
                            if (continuation.isActive) continuation.resume(null) {}
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(null) {}
                    }
                }
            ) { _ -> if (continuation.isActive) continuation.resume(null) {} }
        } ?: return null

        // Phase 2: resolve user avatar
        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        return rawUser
    }
}
