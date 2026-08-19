package com.minhtu.firesocialmedia.data.remote.service.auth.notification

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
