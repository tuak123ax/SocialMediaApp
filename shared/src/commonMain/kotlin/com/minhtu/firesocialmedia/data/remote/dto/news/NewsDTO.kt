package com.minhtu.firesocialmedia.data.remote.dto.news

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.base.CountInterface
import kotlinx.serialization.Serializable

@Serializable
data class NewsDTO(override var id: String = "",
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

    override fun toMap(): Map<String, Any?> =
        super.toMap() + mapOf(
        "likeCount" to likeCount,
        "commentCount" to commentCount,
        "timePosted" to timePosted
    )
}