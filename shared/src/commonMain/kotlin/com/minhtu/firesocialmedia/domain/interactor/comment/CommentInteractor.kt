package com.minhtu.firesocialmedia.domain.interactor.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance

interface CommentInteractor {
    suspend fun saveComment(selectedNewId: String,
                            commentId : String,
                            instance : BaseNewsInstance) : Boolean
    suspend fun saveSubComment(id : String,
                               selectedNewId : String,
                               parentCommentId : String,
                               instance : BaseNewsInstance) : Boolean
    suspend fun deleteComment(selectedNewId : String,
                              comment: BaseNewsInstance)
    suspend fun deleteSubComment(selectedNewId : String,
                                 parentCommentId: String,
                                 comment: BaseNewsInstance)
    suspend fun getAllComments(newsId: String) : List<CommentInstance>?
}