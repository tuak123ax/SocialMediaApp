package com.minhtu.firesocialmedia.instance

import java.io.Serializable

enum class NotificationType{
    NONE,
    COMMENT,
    LIKE,
    ADD_FRIEND
}
data class NotificationInstance(
    val id : String = "",
    var content : String ="",
    val avatar : String ="",
    val sender : String = "",
    val timeSend : Long = 0,
    val type : NotificationType = NotificationType.NONE
):Serializable{
    fun updateContent(content : String) {
        this.content = content
    }
}
