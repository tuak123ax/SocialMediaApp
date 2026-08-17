package com.minhtu.firesocialmedia.domain.repository.group
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
    suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean
}
