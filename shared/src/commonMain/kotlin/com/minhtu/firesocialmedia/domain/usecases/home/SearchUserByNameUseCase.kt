package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.UserRepository

class SearchUserByNameUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String,
                                path: String): List<UserInstance>? {
        return userRepository.searchUserByName(name, path)
    }
}