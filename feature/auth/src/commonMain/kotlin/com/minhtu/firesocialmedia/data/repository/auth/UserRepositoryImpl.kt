package com.minhtu.firesocialmedia.data.repository.auth
import com.minhtu.firesocialmedia.data.remote.auth.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.auth.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.auth.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val authDatabaseService: AuthDatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return authDatabaseService.getUser(userId)
    }

    override suspend fun getCurrentUserUid(): String? = authService.getCurrentUserUid()
}
