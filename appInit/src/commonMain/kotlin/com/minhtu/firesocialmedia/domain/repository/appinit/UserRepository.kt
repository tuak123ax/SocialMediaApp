package com.minhtu.firesocialmedia.domain.repository.appinit
import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
    suspend fun searchUserByName(name: String): List<UserDTO>?
}
