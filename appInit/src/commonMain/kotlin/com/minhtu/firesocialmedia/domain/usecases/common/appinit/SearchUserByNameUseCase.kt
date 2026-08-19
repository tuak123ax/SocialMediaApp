package com.minhtu.firesocialmedia.domain.usecases.common.appinit
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser

class SearchUserByNameUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): List<UserInstance> {
        return userRepository.searchUserByName(name)?.map { it.toSearchUser() } ?: emptyList()
    }
}
