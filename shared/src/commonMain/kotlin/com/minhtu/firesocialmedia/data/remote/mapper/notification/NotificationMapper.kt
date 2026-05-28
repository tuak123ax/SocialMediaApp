package com.minhtu.firesocialmedia.data.remote.mapper.notification

import com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationType

fun NotificationDTO.toDomain() : NotificationInstance {
    return NotificationInstance(
        id,
        content,
        avatar,
        sender,
        timeSend,
        convertTypeStringToEnum(type),
        relatedInfo,
        beRead
    )
}

fun convertTypeStringToEnum(type : String) : NotificationType {
    return when(type) {
        "COMMENT" -> {
            NotificationType.COMMENT
        }
        "LIKE" -> {
            NotificationType.LIKE
        }
        "ADD_FRIEND" -> {
            NotificationType.ADD_FRIEND
        }
        "UPLOAD_NEW" -> {
            NotificationType.UPLOAD_NEW
        }
        "SHARE_NEW" -> {
            NotificationType.SHARE_NEW
        }

        "INVITE_TO_GROUP" -> {
            NotificationType.INVITE_TO_GROUP
        }

        else -> {
            NotificationType.NONE
        }
    }
}

fun NotificationInstance.toDto() : NotificationDTO {
    return NotificationDTO(
        id,
        content,
        avatar,
        sender,
        timeSend,
        type.name,
        relatedInfo,
        beRead
    )
}