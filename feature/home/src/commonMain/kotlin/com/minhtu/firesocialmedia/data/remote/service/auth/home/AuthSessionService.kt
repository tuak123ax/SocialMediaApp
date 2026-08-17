package com.minhtu.firesocialmedia.data.remote.service.auth.home

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
