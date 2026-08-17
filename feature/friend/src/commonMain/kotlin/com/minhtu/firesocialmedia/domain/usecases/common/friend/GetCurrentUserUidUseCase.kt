package com.minhtu.firesocialmedia.domain.usecases.common.friend
import com.minhtu.firesocialmedia.domain.repository.friend.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
