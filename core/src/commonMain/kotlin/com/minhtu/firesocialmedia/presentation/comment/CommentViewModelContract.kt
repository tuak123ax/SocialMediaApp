package com.minhtu.firesocialmedia.presentation.comment

import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for the comment feature view model.
 *
 * Lives in :shared so shared UI code can stay feature-module agnostic.
 * Concrete implementation is provided by :feature:comment.
 */
interface CommentViewModelContract {
    val allComments: StateFlow<ArrayList<CommentInstance>>
    var message: String
    val messageFlow: StateFlow<String>
    val createCommentStatus: StateFlow<Boolean?>
    val commentBeReplied: StateFlow<CommentInstance?>
    val likedComments: StateFlow<HashMap<String, Int>>
    val likeCountList: StateFlow<HashMap<String, Int>>

    fun updateMessage(input: String)
    fun sendComment(currentUser: UserInstance, selectedNew: NewsInstance)
    fun resetCommentStatus()
    fun copyToClipboard(text: String, platform: PlatformContext)
    fun updateCommentBeReplied(value: CommentInstance?)
    fun onLikeComment(selectedNew: NewsInstance, currentUser: UserInstance, comment: CommentInstance)
    fun updateLikeStatus()
    fun updateLikeCommentOfCurrentUser(currentUser: UserInstance)
    fun onDeleteComment(selectedNew: NewsInstance, comment: CommentInstance)
    suspend fun findUserById(userId: String): UserInstance?
    fun getAllCommentsOfNew(newsId: String)
    fun clearCommentList()
}