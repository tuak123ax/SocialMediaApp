package com.minhtu.firesocialmedia.data.remote.service.auth.security

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
