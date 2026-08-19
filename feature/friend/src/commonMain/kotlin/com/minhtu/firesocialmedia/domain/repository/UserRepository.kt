package com.minhtu.firesocialmedia.domain.repository
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
}
