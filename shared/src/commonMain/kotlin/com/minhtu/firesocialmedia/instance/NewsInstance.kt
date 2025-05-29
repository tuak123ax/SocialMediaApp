package com.minhtu.firesocialmedia.instance

import kotlinx.serialization.Serializable

@Serializable
data class NewsInstance(override var id: String = "",
                        override var posterId : String = "",
                        override var posterName: String = "",
                        override var avatar: String = "",
                        override var message: String = "",
                        override var image: String = "",
                        override var video: String = "",
                        var isVisible: Boolean = true,
                        override var likeCount: Int = 0,
                        override var commentCount: Int = 0,
                        override var timePosted: Long = 0): BaseNewsInstance,
    CountInterface {
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
    override fun updateImage(image: String) {
        this.image = image
    }

    override fun updateVideo(video: String) {
        this.video = video
    }

    override fun increaseLikeCount() {
        likeCount++
    }

    override fun increaseCommentCount() {
        commentCount++
    }

    override fun decreaseLikeCount() {
        likeCount--
    }

    override fun decreaseCommentCount() {
        commentCount--
    }

    fun toMap(): Map<String, Any> = mapOf(
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