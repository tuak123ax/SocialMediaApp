package com.minhtu.firesocialmedia.domain.usecases.common.appinit
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository

class GetCurrentUserUidUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getCurrentUserUid()
    }
}
