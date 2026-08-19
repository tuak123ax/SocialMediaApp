package com.minhtu.firesocialmedia.comment.entity.notification

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance as NotificationSharedInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType as NotificationSharedType

fun NotificationInstance.toSharedNotification(): NotificationSharedInstance = NotificationSharedInstance(
    id = id, content = content, avatar = avatar, sender = sender,
    timeSend = timeSend,
    type = when (type) {
        NotificationType.COMMENT -> NotificationSharedType.COMMENT
        NotificationType.LIKE -> NotificationSharedType.LIKE
        NotificationType.ADD_FRIEND -> NotificationSharedType.ADD_FRIEND
        NotificationType.UPLOAD_NEW -> NotificationSharedType.UPLOAD_NEW
        NotificationType.SHARE_NEW -> NotificationSharedType.SHARE_NEW
        NotificationType.INVITE_TO_GROUP -> NotificationSharedType.INVITE_TO_GROUP
        NotificationType.NONE -> NotificationSharedType.NONE
    },
    relatedInfo = relatedInfo, beRead = beRead
)
