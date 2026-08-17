package com.minhtu.firesocialmedia.domain.usecases.common.profile
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
