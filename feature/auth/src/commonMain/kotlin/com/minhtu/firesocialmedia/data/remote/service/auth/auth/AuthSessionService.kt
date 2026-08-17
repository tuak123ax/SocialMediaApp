package com.minhtu.firesocialmedia.data.remote.service.auth.auth

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
