package com.minhtu.firesocialmedia.data.local.service.room

/**
 * Plain transport shape mirroring feature/comment's Room `CommentEntity` fields, without any Room
 * annotations. Unlike User/News/Notification, comment's domain instance (`CommentInstance`) is not
 * a core type — it's owned by feature/comment itself — so this record exists purely so
 * [CommentRoomService]'s contract has a stable shape; feature/comment converts
 * CommentInstance <-> CommentRoomRecord <-> CommentEntity at its own boundaries.
 */
data class CommentRoomRecord(
    val id: String = "",
    val posterId: String = "",
    val posterName: String = "",
    val avatar: String = "",
    val message: String = "",
    val video: String = "",
    val image: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val timePosted: Long = 0,
    val selectedNewId: String = ""
)

/**
 * Room-backed local storage contract for Comment data.
 *
 * Feature-owned: comment's Room infra (CommentEntity/CommentDao) and every consumer of this
 * interface (CommentDbRepositoryImpl, ClearCommentLocalDataUseCaseImpl) live in feature/comment.
 * Cross-feature cleanup (feature/security's ClearLocalDataUseCase) never imports this type
 * directly — it only depends on the generic LocalDataCleaner contract, wired together in appInit.
 */
interface CommentRoomService {
    suspend fun saveComment(comment: CommentRoomRecord)
    suspend fun getAllComments(): List<CommentRoomRecord>
    suspend fun clearComments()
    suspend fun hasComment(): Boolean
}
