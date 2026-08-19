package com.minhtu.firesocialmedia.data.local.mapper.room

import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

fun List<NotificationInstance>.toNotificationEntity() : List<NotificationEntity> {
    return this.map {
        it.toRoomEntity()
    }
}

fun NotificationInstance.toRoomEntity() : NotificationEntity {
    return NotificationEntity(
        id,
        content,
        avatar,
        sender,
        timeSend,
        type,
        relatedInfo,
        beRead
    )
}

fun List<NotificationEntity>.toNotificationInstance() : List<NotificationInstance> {
    return this.map {
        it.toDomain()
    }
}

fun NotificationEntity.toDomain() : NotificationInstance {
    return NotificationInstance(
        id,
        content,
        avatar,
        sender,
        timeSend,
        type,
        relatedInfo,
        beRead
    )
}
