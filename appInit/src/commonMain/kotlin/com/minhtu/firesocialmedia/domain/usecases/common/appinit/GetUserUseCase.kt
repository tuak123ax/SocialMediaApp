package com.minhtu.firesocialmedia.domain.usecases.common.appinit
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, isCurrentUser: Boolean): UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)?.toSearchUser()
    }
}
