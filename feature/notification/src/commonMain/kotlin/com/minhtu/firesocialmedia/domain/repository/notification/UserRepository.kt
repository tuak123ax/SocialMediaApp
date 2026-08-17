package com.minhtu.firesocialmedia.domain.repository.notification
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
}
