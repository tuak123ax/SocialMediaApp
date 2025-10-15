package com.minhtu.firesocialmedia.data.local.mapper.room

import com.minhtu.firesocialmedia.data.local.entity.NewEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

fun List<UserInstance?>.toUserEntity() : List<UserEntity> {
    return this.mapNotNull {
        it?.toRoomEntity()
    }
}

fun UserInstance.toRoomEntity() : UserEntity {
    return UserEntity(
        email,
        image,
        name,
        status,
        token,
        uid
    )
}

fun List<NewsInstance>.toNewEntity() : List<NewEntity> {
    return this.map {
        it.toRoomEntity()
    }
}
fun NewsInstance.toRoomEntity() : NewEntity {
    return NewEntity(
        id,
        posterId,
        posterName,
        avatar,
        message,
        image,
        video,
        isVisible,
        likeCount,
        commentCount,
        timePosted
    )
}

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
        relatedInfo
    )
}