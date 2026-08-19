package com.minhtu.firesocialmedia.home.entity.comment.base

/**
 * Feature-home-owned clone of feature/comment's `domain.entity.base.BaseNewsInstance`, cloned so
 * that feature/home no longer depends on feature/comment (see instruction.md, "duplication over
 * shared interface" convention already used for home's local News stack).
 */
interface BaseNewsInstance {
    val id: String
    val posterId : String
    val posterName: String
    val avatar: String
    val message: String
    val image: String
    fun updateImage(image : String)
    val video : String
    fun updateVideo(video : String)

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "posterId" to posterId,
        "posterName" to posterName,
        "avatar" to avatar,
        "message" to message,
        "image" to image,
        "video" to video
    )
}
