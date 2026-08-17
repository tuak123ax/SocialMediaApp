package com.minhtu.firesocialmedia.domain.repository.auth
import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
}
