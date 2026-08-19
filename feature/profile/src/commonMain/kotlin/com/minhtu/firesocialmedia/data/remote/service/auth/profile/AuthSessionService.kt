package com.minhtu.firesocialmedia.data.remote.service.auth.profile

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
