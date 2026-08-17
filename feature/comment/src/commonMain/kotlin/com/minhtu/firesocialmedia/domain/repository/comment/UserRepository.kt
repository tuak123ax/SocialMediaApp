package com.minhtu.firesocialmedia.domain.repository.comment
import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
}
