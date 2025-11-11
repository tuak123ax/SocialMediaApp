package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteSubCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.GetAllCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveSubCommentToDatabaseUseCase

class CommentInteractorImpl(
    private val saveCommentToDatabaseUseCase: SaveCommentToDatabaseUseCase,
    private val saveSubCommentToDatabaseUseCase: SaveSubCommentToDatabaseUseCase,
    private val deleteCommentFromDatabaseUseCase: DeleteCommentFromDatabaseUseCase,
    private val deleteSubCommentFromDatabaseUseCase: DeleteSubCommentFromDatabaseUseCase,
    private val getAllCommentsUseCase : GetAllCommentsUseCase
) : CommentInteractor {
    override suspend fun saveComment(
        selectedNewId: String,
        commentId : String,
        instance : CommentInstance) : Boolean {
        return saveCommentToDatabaseUseCase.invoke(
            selectedNewId,
            commentId,
            instance
        )
    }

    override suspend fun saveSubComment(
        id: String,
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ): Boolean {
        return saveSubCommentToDatabaseUseCase.invoke(
            id,
            selectedNewId,
            parentCommentId,
            instance
        )
    }

    override suspend fun deleteComment(
        selectedNewId : String,
        comment: BaseNewsInstance
    ) {
        deleteCommentFromDatabaseUseCase.invoke(
            selectedNewId,
            comment
        )
    }

    override suspend fun deleteSubComment(
        selectedNewId: String,
        parentCommentId : String,
        comment: BaseNewsInstance
    ) {
        deleteSubCommentFromDatabaseUseCase.invoke(
            selectedNewId,
            parentCommentId,
            comment
        )
    }

    override suspend fun getAllComments(
        newsId: String
    ): List<CommentInstance> {
        return getAllCommentsUseCase.invoke(
            newsId
        )
    }
}