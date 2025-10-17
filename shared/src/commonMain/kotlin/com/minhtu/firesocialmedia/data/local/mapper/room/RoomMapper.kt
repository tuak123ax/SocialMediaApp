package com.minhtu.firesocialmedia.data.local.mapper.room

import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
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

fun UserEntity.toDomain() : UserInstance {
    return UserInstance(
        email,
        image,
        name,
        status,
        token,
        uid
    )
}

fun List<NewsInstance>.toNewEntity() : List<NewsEntity> {
    return this.map {
        it.toRoomEntity()
    }
}
fun NewsInstance.toRoomEntity() : NewsEntity {
    return NewsEntity(
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

fun List<NewsEntity>.toDomain() : List<NewsInstance> {
    return this.map {
        it.toDomain()
    }
}

fun NewsEntity.toDomain() : NewsInstance {
    return NewsInstance(
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
        relatedInfo
    )
}