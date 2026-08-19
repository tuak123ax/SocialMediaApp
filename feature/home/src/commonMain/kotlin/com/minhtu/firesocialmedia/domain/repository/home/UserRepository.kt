package com.minhtu.firesocialmedia.domain.repository.home
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
    suspend fun searchUserByName(name: String): List<UserDTO>?
    suspend fun updateFCMTokenForCurrentUser(user: UserDTO)
}
