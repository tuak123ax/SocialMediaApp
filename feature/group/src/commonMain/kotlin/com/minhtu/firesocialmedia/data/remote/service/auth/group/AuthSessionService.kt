package com.minhtu.firesocialmedia.data.remote.service.auth.group

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
