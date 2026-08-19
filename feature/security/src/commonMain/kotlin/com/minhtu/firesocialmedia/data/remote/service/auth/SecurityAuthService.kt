package com.minhtu.firesocialmedia.data.remote.service.auth

interface SecurityAuthService {
    suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean

    suspend fun changePassword(
        userId: String,
        newPassword: String,
        userPath: String,
        lastTimeChangePasswordPath: String
    ): Result<Unit>
}
