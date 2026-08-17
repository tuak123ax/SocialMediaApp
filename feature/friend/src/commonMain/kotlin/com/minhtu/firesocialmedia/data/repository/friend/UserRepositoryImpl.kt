package com.minhtu.firesocialmedia.data.repository.friend
import com.minhtu.firesocialmedia.friend.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.auth.friend.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.FriendDatabaseService
import com.minhtu.firesocialmedia.domain.repository.friend.UserRepository

class UserRepositoryImpl(
    private val authService: AuthSessionService,
    private val friendDatabaseService: FriendDatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return friendDatabaseService.getUser(userId)
    }

    override suspend fun getCurrentUserUid(): String? = authService.getCurrentUserUid()
}
