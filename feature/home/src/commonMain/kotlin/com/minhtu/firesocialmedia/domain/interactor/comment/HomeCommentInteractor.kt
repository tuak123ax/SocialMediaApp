package com.minhtu.firesocialmedia.domain.interactor.comment

import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance

/**
 * Feature-home-owned clone of feature/comment's `domain.interactor.comment.CommentInteractor`.
 * Named with a `Home` prefix because it shares its package with feature/comment's own
 * `CommentInteractor` (split package across Gradle modules is fine as long as simple class names
 * don't collide at the final `:Fire_Social_Media:assembleDebug` DEX-merge step).
 */
interface HomeCommentInteractor {
    suspend fun saveComment(selectedNewId: String,
                            commentId : String,
                            instance : CommentInstance) : Boolean
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
