package com.minhtu.firesocialmedia.data.remote.dto.news

import kotlinx.serialization.Serializable

@Serializable
data class NewsDTO(var id: String = "",
                        var posterId : String = "",
                        var posterName: String = "",
                        var avatar: String = "",
                        var message: String = "",
                        var image: String = "",
                        var video: String = "",
                        var isVisible: Boolean = true,
                        var likeCount: Int = 0,
                        var commentCount: Int = 0,
                        var timePosted: Long = 0,
                        var localPath : String = "",
                        var shareContentId : String = "",
                        var decentralizationType : String = "",
                        var type: String? = null,
                        var pollId: String? = null) {
    fun updateNews(id: String, posterId: String, posterName: String, avatar: String,
                   message: String, image: String, video : String){
        this.id = id
        this.posterId = posterId
        this.posterName = posterName
        this.avatar = avatar
        this.message = message
        this.image = image
        this.video = video
    }
    fun updateImage(image: String) {
        this.image = image
    }

    fun updateVideo(video: String) {
        this.video = video
    }

    fun increaseLikeCount() {
        likeCount++
    }

    fun increaseCommentCount() {
        commentCount++
    }

    fun decreaseLikeCount() {
        likeCount--
    }

    fun decreaseCommentCount() {
        commentCount--
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "posterId" to posterId,
        "posterName" to posterName,
        "avatar" to avatar,
        "message" to message,
        "image" to image,
        "video" to video,
        "likeCount" to likeCount,
        "commentCount" to commentCount,
        "timePosted" to timePosted
    )
}
