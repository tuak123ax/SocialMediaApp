package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO

/**
 * appInit-owned counterpart of core's generic `DatabaseService`, scoped to the User-typed reads
 * that only the search composition-root screen needs (`getUser`/`searchUserByName`). Mirrors the
 * pattern already established by feature/home's `HomeDatabaseService`, feature/friend's
 * `FriendDatabaseService`, etc. — keeps core's `DatabaseService` free of any UserDTO-typed method.
 */
interface AppInitDatabaseService {
    suspend fun getUser(userId: String): UserDTO?

    suspend fun searchUserByName(name: String, path: String): List<UserDTO>?
}
