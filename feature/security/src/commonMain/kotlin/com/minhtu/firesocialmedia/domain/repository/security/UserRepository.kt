package com.minhtu.firesocialmedia.domain.repository.security
interface UserRepository {
    suspend fun getCurrentUserUid(): String?
}
