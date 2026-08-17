package com.minhtu.firesocialmedia.domain.usecases.common.friend
import com.minhtu.firesocialmedia.domain.repository.friend.UserRepository
import com.minhtu.firesocialmedia.friend.entity.user.UserInstance
import com.minhtu.firesocialmedia.friend.entity.user.toFriendUser

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, isCurrentUser: Boolean): UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)?.toFriendUser()
    }
}
