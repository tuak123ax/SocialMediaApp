package com.minhtu.firesocialmedia.core.domain.usecases.common

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId : String,
                                isCurrentUser: Boolean) : UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)
    }
}