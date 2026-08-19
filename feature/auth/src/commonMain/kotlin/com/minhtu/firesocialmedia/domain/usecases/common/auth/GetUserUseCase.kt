package com.minhtu.firesocialmedia.domain.usecases.common.auth
import com.minhtu.firesocialmedia.domain.entity.user.auth.UserInstance
import com.minhtu.firesocialmedia.domain.entity.user.auth.toDomain
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, isCurrentUser: Boolean): UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)?.toDomain()
    }
}
