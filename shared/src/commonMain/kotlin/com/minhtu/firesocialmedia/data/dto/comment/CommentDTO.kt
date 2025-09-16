package com.minhtu.firesocialmedia.data.dto.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.base.CountInterface
import kotlinx.serialization.Serializable

@Serializable
data class CommentDTO(override var id: String = "",
                      override var posterId : String = "",
                      override var posterName: String = "",
                      override var avatar: String = "",
                      override var message: String = "",
                      override var video: String = "",
                      override var image: String = "",
                      var listReplies : HashMap<String, CommentDTO> = HashMap(),
                      override var likeCount: Int = 0,
                      override var commentCount: Int = 0,
                      override var timePosted: Long = 0): BaseNewsInstance,
    CountInterface {
    fun updateListReplies(list : HashMap<String, CommentDTO>) {
        listReplies = list
    }
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

    override fun updateVideo(video: String) {

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
        "timePosted" to timePosted,
        "listReplies" to listReplies
    )
}