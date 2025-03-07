package com.minhtu.firesocialmedia.instance

import java.io.Serializable

data class CommentInstance(override var id: String = "",
                        override var posterId : String = "",
                        override var posterName: String = "",
                        override var avatar: String = "",
                        override var message: String = "",
                        override var image: String = ""): Serializable, BaseNewsInstance,
    CountInterface {
    fun updateComments(id: String, posterId: String, posterName: String, avatar: String,
                   message: String, image: String){
        this.id = id
        this.posterId = posterId
        this.posterName = posterName
        this.avatar = avatar
        this.message = message
        this.image = image
    }
    override fun updateImage(image: String) {
        this.image = image
    }

    override var likeCount: Int = 0
    override var commentCount: Int = 0
    override var timePosted: Long = 0

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
}