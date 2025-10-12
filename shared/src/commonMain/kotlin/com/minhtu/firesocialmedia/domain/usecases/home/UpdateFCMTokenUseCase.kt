package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.UserRepository

class UpdateFCMTokenUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user : UserInstance) {
        userRepository.updateFCMTokenForCurrentUser(user)
    }
}