package com.minhtu.firesocialmedia.data.remote.service.auth

interface ProfileAuthService {
    suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean
}
