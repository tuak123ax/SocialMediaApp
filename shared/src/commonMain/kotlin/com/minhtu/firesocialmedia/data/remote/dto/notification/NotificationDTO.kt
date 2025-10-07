package com.minhtu.firesocialmedia.data.remote.dto.notification

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDTO(
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

fun NotificationDTO.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "content" to content,
    "avatar" to avatar,
    "sender" to sender,
    "timeSend" to timeSend,
    "type" to type.name, // store enum as string
    "relatedInfo" to relatedInfo
)

fun NotificationDTO.Companion.fromMap(map: Map<String, Any?>): NotificationDTO {
    return NotificationDTO(
        id = map["id"] as? String ?: "",
        content = map["content"] as? String ?: "",
        avatar = map["avatar"] as? String ?: "",
        sender = map["sender"] as? String ?: "",
        timeSend = (map["timeSend"] as? Number)?.toLong() ?: 0L,
        type = (map["type"] as? String)?.let { NotificationType.valueOf(it) } ?: NotificationType.NONE,
        relatedInfo = map["relatedInfo"] as? String ?: ""
    )
}
