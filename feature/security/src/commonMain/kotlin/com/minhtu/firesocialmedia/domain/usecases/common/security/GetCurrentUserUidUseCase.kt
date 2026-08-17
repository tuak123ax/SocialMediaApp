package com.minhtu.firesocialmedia.domain.usecases.common.security
import com.minhtu.firesocialmedia.domain.repository.security.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
