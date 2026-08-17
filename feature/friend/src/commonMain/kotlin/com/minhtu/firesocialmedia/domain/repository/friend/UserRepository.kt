package com.minhtu.firesocialmedia.domain.repository.friend
import com.minhtu.firesocialmedia.friend.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
}
