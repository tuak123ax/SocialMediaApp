package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.home.entity.user.toHomeUser
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository

class SearchUserByNameUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): List<UserInstance>? {
        return userRepository.searchUserByName(name)?.map { it.toHomeUser() }
    }
}
