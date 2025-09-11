package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.GetAllCommentsUseCase

class CommentInteractorImpl(
    private val saveCommentToDatabaseUseCase: SaveCommentToDatabaseUseCase,
    private val deleteCommentFromDatabaseUseCase: DeleteCommentFromDatabaseUseCase,
    private val getAllCommentsUseCase : GetAllCommentsUseCase
) : CommentInteractor {
    override suspend fun saveComment(
        id: String,
        path: String,
        instance: BaseNewsInstance) : Boolean {
        return saveCommentToDatabaseUseCase.invoke(
            id,
            path,
            instance
        )
    }

    override suspend fun deleteComment(
        path: String,
        comment: BaseNewsInstance
    ) {
        deleteCommentFromDatabaseUseCase.invoke(
            path,
            comment
        )
    }

    override suspend fun getAllComments(
        path: String,
        newsId: String
    ): List<CommentInstance> {
        return getAllCommentsUseCase.invoke(
            path,
            newsId
        )
    }
}