package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO

/**
 * feature/friend's own database service, holding only the User-related query it needs
 * (mirroring feature/home's/feature/auth's own [UserDTO] fork and the same
 * `<Feature>DatabaseService` convention).
 */
interface FriendDatabaseService {
    suspend fun getUser(userId: String): UserDTO?

    suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    )
}
