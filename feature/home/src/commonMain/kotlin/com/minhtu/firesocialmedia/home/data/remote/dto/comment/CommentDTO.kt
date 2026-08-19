package com.minhtu.firesocialmedia.home.data.remote.dto.comment

import kotlinx.serialization.Serializable

@Serializable
data class CommentDTO(var id: String = "",
                      var posterId : String = "",
                      var posterName: String = "",
                      var avatar: String = "",
                      var message: String = "",
                      var video: String = "",
                      var image: String = "",
                      var listReplies : HashMap<String, CommentDTO> = HashMap(),
                      var likeCount: Int = 0,
                      var commentCount: Int = 0,
                      var timePosted: Long = 0,
                      var selectedNewId : String = "") {
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
    fun updateImage(image: String) {
        this.image = image
    }

    fun updateVideo(video: String) {

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
        "timePosted" to timePosted,
        "listReplies" to listReplies
    )
}
