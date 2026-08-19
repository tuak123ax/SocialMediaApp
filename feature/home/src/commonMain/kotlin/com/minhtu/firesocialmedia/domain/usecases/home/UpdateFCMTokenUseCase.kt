package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.home.entity.user.toDto
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository

class UpdateFCMTokenUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user : UserInstance) {
        userRepository.updateFCMTokenForCurrentUser(user.toDto())
    }
}
