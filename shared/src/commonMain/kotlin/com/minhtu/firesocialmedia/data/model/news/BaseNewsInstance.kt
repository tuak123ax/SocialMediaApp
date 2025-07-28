package com.minhtu.firesocialmedia.data.model.news

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
}

fun BaseNewsInstance.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "posterId" to posterId,
    "posterName" to posterName,
    "avatar" to avatar,
    "message" to message,
    "image" to image,
    "video" to video
)