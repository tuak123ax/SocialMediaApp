package com.minhtu.firesocialmedia.data.remote.service.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.constants.search.DataConstant
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.SupabaseStorageHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine

private fun Map<*, *>.toAppInitUserDTO(): UserDTO {
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
 * iOS implementation of appInit's own [AppInitDatabaseService], mirrors feature/home's
 * `IosHomeDatabaseService.getUser`/`searchUserByName` exactly.
 */
class IosAppInitDatabaseService : AppInitDatabaseService {
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
                        val value = snapshot.value as? Map<*, *> ?: null
                        if (value != null) {
                            try {
                                val user = value.toAppInitUserDTO()
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

        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        return rawUser
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        val rawList = suspendCancellableCoroutine<List<UserDTO>?> { continuation ->
            val databaseReference = database.child(DataConstant.USER_PATH)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val users = mutableListOf<UserDTO>()
                        val children = snapshot.children

                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue

                            try {
                                val user = value.toAppInitUserDTO()
                                if (user.name.contains(name, ignoreCase = true)) {
                                    users.add(user)
                                    if (users.size >= 5) break // only return first 5 matches
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }

                        continuation.resume(users) {}
                    } else {
                        continuation.resume(emptyList<UserDTO>()) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        return coroutineScope {
            rawList.map { user ->
                async {
                    if (user.image.isNotEmpty()) user.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(user.image))
                    user
                }
            }.awaitAll()
        }
    }
}
