package com.minhtu.firesocialmedia.domain.entity.base

import com.minhtu.firesocialmedia.data.local.entity.CommentEntity
import kotlin.String

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