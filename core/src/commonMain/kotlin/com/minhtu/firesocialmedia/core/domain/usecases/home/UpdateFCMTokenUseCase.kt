package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository

class UpdateFCMTokenUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user : UserInstance) {
        userRepository.updateFCMTokenForCurrentUser(user)
    }
}