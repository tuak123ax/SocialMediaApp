package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO

/**
 * Feature-scoped counterpart of core's `DatabaseService`, mirroring feature/home's
 * `HomeDatabaseService` pattern: keeps core's `DatabaseService` free of any
 * Comment-typed methods while feature/comment retains direct access to the
 * underlying data source.
 */
interface CommentDatabaseService {
    suspend fun getAllComments(path: String, newsId: String): List<CommentDTO>?
    suspend fun getUser(userId: String): UserDTO?

    suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    )

    suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean
}
