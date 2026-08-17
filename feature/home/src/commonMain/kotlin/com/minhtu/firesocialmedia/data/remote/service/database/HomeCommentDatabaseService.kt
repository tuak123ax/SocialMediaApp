package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.home.data.remote.dto.comment.CommentDTO

/**
 * Home's own independent counterpart of feature/comment's `CommentDatabaseService`,
 * mirroring `HomeDatabaseService`'s pattern. Kept entirely within :feature:home so
 * home no longer needs `implementation(project(":feature:comment"))` (see instruction.md).
 */
interface HomeCommentDatabaseService {
    suspend fun getAllComments(path: String, newsId: String): List<CommentDTO>?

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
