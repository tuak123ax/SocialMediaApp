package com.minhtu.firesocialmedia.domain.usecases.common.notification
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
