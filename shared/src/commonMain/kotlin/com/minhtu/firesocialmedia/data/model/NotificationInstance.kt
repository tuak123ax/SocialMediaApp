package com.minhtu.firesocialmedia.data.model

import kotlinx.serialization.Serializable

enum class NotificationType{
    NONE,
    COMMENT,
    LIKE,
    ADD_FRIEND,
    UPLOAD_NEW
}
@Serializable
data class NotificationInstance(
    val id : String = "",
    var content : String ="",
    val avatar : String ="",
    val sender : String = "",
    val timeSend : Long = 0,
    val type : NotificationType = NotificationType.NONE,
    var relatedInfo : String = ""
){
    companion object{

    }
    fun updateContent(content : String) {
        this.content = content
    }
    fun updateRelatedInfo(info : String) {
        relatedInfo = info
    }
}

fun NotificationInstance.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "content" to content,
    "avatar" to avatar,
    "sender" to sender,
    "timeSend" to timeSend,
    "type" to type.name, // store enum as string
    "relatedInfo" to relatedInfo
)

fun NotificationInstance.Companion.fromMap(map: Map<String, Any?>): NotificationInstance {
    return NotificationInstance(
        id = map["id"] as? String ?: "",
        content = map["content"] as? String ?: "",
        avatar = map["avatar"] as? String ?: "",
        sender = map["sender"] as? String ?: "",
        timeSend = (map["timeSend"] as? Number)?.toLong() ?: 0L,
        type = (map["type"] as? String)?.let { NotificationType.valueOf(it) } ?: NotificationType.NONE,
        relatedInfo = map["relatedInfo"] as? String ?: ""
    )
}
