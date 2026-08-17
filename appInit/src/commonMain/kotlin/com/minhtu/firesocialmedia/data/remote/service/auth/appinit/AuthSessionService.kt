package com.minhtu.firesocialmedia.data.remote.service.auth.appinit

interface AuthSessionService {
    suspend fun getCurrentUserUid(): String?
    suspend fun getCurrentUserEmail(): String?
}
