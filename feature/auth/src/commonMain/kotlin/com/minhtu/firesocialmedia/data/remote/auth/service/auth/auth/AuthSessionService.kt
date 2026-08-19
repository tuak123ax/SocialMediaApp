package com.minhtu.firesocialmedia.data.remote.auth.service.auth.auth

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
