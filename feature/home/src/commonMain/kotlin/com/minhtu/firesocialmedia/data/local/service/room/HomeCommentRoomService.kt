package com.minhtu.firesocialmedia.data.local.service.room

/**
 * Plain transport shape for feature/home's own, independently-cloned comment stack
 * (HomeCommentEntity/HomeCommentDao). A local clone of feature/comment's CommentRoomRecord shape
 * — feature/home has no Gradle dependency on feature/comment, and this DTO carries no Room
 * annotations or feature-owned types, so duplicating the plain shape here is simpler than
 * threading a shared type across a module boundary.
 */
data class HomeCommentRoomRecord(
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
 * Home's own local Room persistence contract for its independently-cloned comment stack,
 * distinct from feature/comment's [com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService].
 */
interface HomeCommentRoomService {
    suspend fun saveComment(comment: HomeCommentRoomRecord)
    suspend fun getAllComments(): List<HomeCommentRoomRecord>
    suspend fun clearComments()
    suspend fun hasComment(): Boolean
}
