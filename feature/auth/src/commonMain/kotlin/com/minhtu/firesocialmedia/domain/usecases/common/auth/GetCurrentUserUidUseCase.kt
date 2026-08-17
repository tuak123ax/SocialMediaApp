package com.minhtu.firesocialmedia.domain.usecases.common.auth
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
