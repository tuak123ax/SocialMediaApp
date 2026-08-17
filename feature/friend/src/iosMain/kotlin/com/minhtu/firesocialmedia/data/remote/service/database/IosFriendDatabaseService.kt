package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.constants.friend.DataConstant
import com.minhtu.firesocialmedia.friend.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.friend.IosDatabaseHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import kotlinx.coroutines.suspendCancellableCoroutine

private fun Map<*, *>.toFriendUserDTO(): UserDTO {
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

/**
 * iOS implementation of feature/friend's own [FriendDatabaseService], porting only the
 * `getUser` query from core's `IosDatabaseService`. Media (image) URL resolution still goes
 * through core's generic [SupabaseStorageHelper] companion function, which remains shared infra.
 */
class IosFriendDatabaseService : FriendDatabaseService {
    private val database: FIRDatabaseReference
        get() = FIRDatabase.database().reference()

    override suspend fun getUser(userId: String): UserDTO? {
        val rawUser = suspendCancellableCoroutine<UserDTO?> { continuation ->
            val databaseReference = database
                .child(DataConstant.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *>
                        if (value != null) {
                            try {
                                val user = value.toFriendUserDTO()
                                continuation.resume(user) {}
                            } catch (_: Exception) {
                                continuation.resume(null) {}
                            }
                        } else {
                            continuation.resume(null) {}
                        }
                    } else {
                        continuation.resume(null) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        // Phase 2: resolve user avatar
        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        return rawUser
    }

    override suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    ) {
        IosDatabaseHelper.saveListToDatabase(id, path, value, externalPath)
    }
}
