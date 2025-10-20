package com.minhtu.firesocialmedia.domain.usecases.common

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId : String,
                                isCurrentUser: Boolean) : UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)
    }
}