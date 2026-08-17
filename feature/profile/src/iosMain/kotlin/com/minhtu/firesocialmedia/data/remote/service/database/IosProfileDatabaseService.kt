package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.profile.IosDatabaseHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.profile.SupabaseStorage
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private fun Map<String, Any?>.toProfileNewsDTO(): NewsDTO {
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
        groupId = this["groupId"] as? String ?: "",
        type = this["type"] as? String,
        pollId = this["pollId"] as? String
    )
}

private fun Map<*, *>.toProfileUserDTO(): UserDTO {
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

class IosProfileDatabaseService : ProfileDatabaseService {
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
                            if (continuation.isActive) continuation.resume(value.toProfileNewsDTO()) {}
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

    override suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            FIRDatabase.database().reference()
                .child(newsPath)
                .child(new.id)
                .removeValueWithCompletionBlock { error, _ ->
                    if (continuation.isActive) continuation.resume(error == null)
                }
        }

    override suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {
        if (value < 0) return
        FIRDatabase.database().reference()
            .child(newsPath)
            .child(newsId)
            .child(likedCountPath)
            .setValue(value.toLong()) { _, _ -> }
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to null,
            "$pollPath/$pollId" to null,
            "$pollVotesPath/$pollId" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
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
                                val user = value.toProfileUserDTO()
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

    override suspend fun saveValueToDatabase(id: String, path: String, value: HashMap<String, Int>, externalPath: String): Boolean {
        return IosDatabaseHelper.saveValueToDatabase(id, path, value, externalPath)
    }

    override suspend fun saveListToDatabase(id: String, path: String, value: ArrayList<String>, externalPath: String) {
        IosDatabaseHelper.saveListToDatabase(id, path, value, externalPath)
    }

    override suspend fun updateUserStringField(userId: String, fieldPath: String, value: String, userPath: String): Boolean =
        suspendCancellableCoroutine { cont ->
            val ref = FIRDatabase.database().reference()
                .child(userPath).child(userId).child(fieldPath)
            ref.setValue(value) { error, _ ->
                if (cont.isActive) cont.resume(error == null)
            }
        }

    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean {
        return try {
            val remotePath = "avatar/${userId}_${getCurrentTime()}.jpg"
            SupabaseStorage.uploadFile(imageUri, remotePath)
            updateUserStringField(userId, "image", remotePath, userPath)
        } catch (e: Exception) {
            logMessage("updateUserAvatar", { "Failed: ${e.message}" })
            false
        }
    }

    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean {
        return try {
            val remotePath = "background/${userId}_${getCurrentTime()}.jpg"
            SupabaseStorage.uploadFile(imageUri, remotePath)
            updateUserStringField(userId, "background", remotePath, userPath)
        } catch (e: Exception) {
            logMessage("updateUserBackground", { "Failed: ${e.message}" })
            false
        }
    }

    override suspend fun anyChildMatchesFieldValue(path: String, fields: List<String>, value: String): Boolean? =
        suspendCancellableCoroutine { continuation ->
            val ref = FIRDatabase.database().reference().child(path)
            ref.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    var matched = false
                    if (snapshot != null && snapshot.exists()) {
                        val children = snapshot.children
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            for (field in fields) {
                                val fieldValue = child.childSnapshotForPath(field).value as? String
                                if (fieldValue == value) {
                                    matched = true
                                    break
                                }
                            }
                            if (matched) break
                        }
                    }
                    if (continuation.isActive) continuation.resume(matched)
                }
            ) { _ -> if (continuation.isActive) continuation.resume(false) }
        }
}
