package com.minhtu.firesocialmedia.data.repository.comment
import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.database.CommentDatabaseService
import com.minhtu.firesocialmedia.domain.repository.comment.UserRepository

class UserRepositoryImpl(
    private val commentDatabaseService: CommentDatabaseService
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        return commentDatabaseService.getUser(userId)
    }
}
