package com.minhtu.firesocialmedia.core.domain.usecases.common

import com.minhtu.firesocialmedia.core.domain.repository.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() : String? {
        return userRepository.getCurrentUserUid()
    }
}