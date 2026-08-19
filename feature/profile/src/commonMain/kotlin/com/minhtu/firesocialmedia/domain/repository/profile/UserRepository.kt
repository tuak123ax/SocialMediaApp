package com.minhtu.firesocialmedia.domain.repository.profile
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO

interface UserRepository {
    suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO?
    suspend fun getCurrentUserUid(): String?
    suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean
}
