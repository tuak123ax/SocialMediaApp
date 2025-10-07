package com.minhtu.firesocialmedia.data.remote.mapper.notification

import com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

fun NotificationDTO.toDomain() : NotificationInstance {
    return NotificationInstance(
        id,
        content,
        avatar,
        sender,
        timeSend,
        type,
        relatedInfo
    )
}

fun NotificationInstance.toDto() : NotificationDTO {
    return NotificationDTO(
        id,
        content,
        avatar,
        sender,
        timeSend,
        type,
        relatedInfo
    )
}