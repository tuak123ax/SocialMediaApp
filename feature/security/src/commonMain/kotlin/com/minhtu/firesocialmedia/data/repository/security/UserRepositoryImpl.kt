package com.minhtu.firesocialmedia.data.repository.security
import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthSessionService
import com.minhtu.firesocialmedia.domain.repository.security.UserRepository

class UserRepositoryImpl(
    private val authService: AuthSessionService
) : UserRepository {
    override suspend fun getCurrentUserUid(): String? = authService.getCurrentUserUid()
}
