package com.minhtu.firesocialmedia.domain.interactor.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance

interface CommentInteractor {
    suspend fun saveComment(id : String,
                            path : String,
                            instance : BaseNewsInstance) : Boolean
    suspend fun deleteComment(path: String,
                              comment: BaseNewsInstance)
    suspend fun getAllComments(path: String,
                               newsId: String) : List<CommentInstance>?
}