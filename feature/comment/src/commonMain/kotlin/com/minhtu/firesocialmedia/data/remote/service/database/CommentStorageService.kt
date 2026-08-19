package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance

interface CommentStorageService {
    suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean

    suspend fun deleteCommentFromDatabase(
        path: String,
        comment: BaseNewsInstance
    )
}
