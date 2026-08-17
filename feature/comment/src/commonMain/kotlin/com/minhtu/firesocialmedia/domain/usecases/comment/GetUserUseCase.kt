package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.comment.entity.user.UserInstance
import com.minhtu.firesocialmedia.comment.entity.user.toCommentUser
import com.minhtu.firesocialmedia.domain.repository.comment.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, isCurrentUser: Boolean): UserInstance? {
        return userRepository.getUser(userId, isCurrentUser)?.toCommentUser()
    }
}
