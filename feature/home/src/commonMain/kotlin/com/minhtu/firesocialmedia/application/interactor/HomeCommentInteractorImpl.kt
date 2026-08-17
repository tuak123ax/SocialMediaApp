package com.minhtu.firesocialmedia.application.interactor

import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.interactor.comment.HomeCommentInteractor
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeDeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeDeleteSubCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeGetAllCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveSubCommentToDatabaseUseCase

class HomeCommentInteractorImpl(
    private val saveCommentToDatabaseUseCase: HomeSaveCommentToDatabaseUseCase,
    private val saveSubCommentToDatabaseUseCase: HomeSaveSubCommentToDatabaseUseCase,
    private val deleteCommentFromDatabaseUseCase: HomeDeleteCommentFromDatabaseUseCase,
    private val deleteSubCommentFromDatabaseUseCase: HomeDeleteSubCommentFromDatabaseUseCase,
    private val getAllCommentsUseCase : HomeGetAllCommentsUseCase
) : HomeCommentInteractor {
    override suspend fun saveComment(
        selectedNewId: String,
        commentId: String,
        instance: CommentInstance
    ): Boolean {
        return saveCommentToDatabaseUseCase(selectedNewId, commentId, instance)
    }

    override suspend fun saveSubComment(
        id: String,
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ): Boolean {
        return saveSubCommentToDatabaseUseCase(id, selectedNewId, parentCommentId, instance)
    }

    override suspend fun deleteComment(selectedNewId: String, comment: BaseNewsInstance) {
        deleteCommentFromDatabaseUseCase(selectedNewId, comment)
    }

    override suspend fun deleteSubComment(
        selectedNewId: String,
        parentCommentId: String,
        comment: BaseNewsInstance
    ) {
        deleteSubCommentFromDatabaseUseCase(selectedNewId, parentCommentId, comment)
    }

    override suspend fun getAllComments(newsId: String): List<CommentInstance> {
        return getAllCommentsUseCase(newsId)
    }
}
