package com.minhtu.firesocialmedia.data.remote.service.auth.friend

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
