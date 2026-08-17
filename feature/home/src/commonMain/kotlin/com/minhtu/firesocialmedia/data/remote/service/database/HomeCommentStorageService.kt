package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance

interface HomeCommentStorageService {
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
