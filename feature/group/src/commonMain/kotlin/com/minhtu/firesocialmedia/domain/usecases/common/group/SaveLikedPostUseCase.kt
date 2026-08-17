package com.minhtu.firesocialmedia.domain.usecases.common.group
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository

class SaveLikedPostUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, likedPosts: HashMap<String, Int>): Boolean {
        return userRepository.saveLikedPost(userId, likedPosts)
    }
}
