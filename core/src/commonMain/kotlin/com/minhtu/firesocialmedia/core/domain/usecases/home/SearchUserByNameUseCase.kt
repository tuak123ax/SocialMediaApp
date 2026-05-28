package com.minhtu.firesocialmedia.core.domain.usecases.home

import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository

class SearchUserByNameUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): List<UserInstance>? {
        return userRepository.searchUserByName(name)
    }
}