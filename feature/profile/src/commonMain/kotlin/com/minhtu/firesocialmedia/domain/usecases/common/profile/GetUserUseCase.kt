package com.minhtu.firesocialmedia.domain.usecases.common.profile
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.profile.entity.user.toProfileUser

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, isCurrentUser: Boolean): UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)?.toProfileUser()
    }
}
