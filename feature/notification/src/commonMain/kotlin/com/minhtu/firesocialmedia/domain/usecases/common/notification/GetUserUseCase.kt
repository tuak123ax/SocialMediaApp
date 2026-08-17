package com.minhtu.firesocialmedia.domain.usecases.common.notification
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDomain
import com.minhtu.firesocialmedia.domain.entity.user.notification.UserInstance
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, isCurrentUser: Boolean): UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)?.toDomain()
    }
}
