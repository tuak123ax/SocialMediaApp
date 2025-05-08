package com.minhtu.firesocialmedia.instance

import java.io.Serializable

enum class NotificationType{
    NONE,
    COMMENT,
    LIKE,
    ADD_FRIEND,
    UPLOAD_NEW
}
data class NotificationInstance(
    val id : String = "",
    var content : String ="",
    val avatar : String ="",
    val sender : String = "",
    val timeSend : Long = 0,
    val type : NotificationType = NotificationType.NONE,
    var relatedInfo : String = ""
):Serializable{
    fun updateContent(content : String) {
        this.content = content
    }
    fun updateRelatedInfo(info : String) {
        relatedInfo = info
    }
}
