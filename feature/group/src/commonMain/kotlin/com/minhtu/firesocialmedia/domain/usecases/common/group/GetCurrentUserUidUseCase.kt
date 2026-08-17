package com.minhtu.firesocialmedia.domain.usecases.common.group
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
