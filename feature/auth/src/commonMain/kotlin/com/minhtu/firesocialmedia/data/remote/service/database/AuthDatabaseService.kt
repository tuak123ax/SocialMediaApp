package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.auth.IpInfoResponseDTO

/**
 * Feature-auth-owned counterpart of core's generic `DatabaseService`, scoped to recording a new
 * login-activity/session entry at sign-in time, and to feature/auth's own user reads/writes
 * (using feature/auth's own [UserDTO] clone rather than core's). This mirrors feature/home's
 * `HomeDatabaseService` pattern: keeps core's `DatabaseService` free of any feature-specific-typed
 * methods while feature/auth retains direct access to the underlying data source.
 */
interface AuthDatabaseService {
    suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    )

    suspend fun getUser(userId: String): UserDTO?

    suspend fun saveSignUpInformation(user: UserDTO): Boolean
}
