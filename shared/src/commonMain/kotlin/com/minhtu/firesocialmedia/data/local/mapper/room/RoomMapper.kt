package com.minhtu.firesocialmedia.data.local.mapper.room

import com.minhtu.firesocialmedia.data.local.entity.CommentEntity
import com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
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
        email, image, name, status, phone, token, uid, background
    )
}

fun UserEntity.toDomain() : UserInstance {
    return UserInstance(
        email = email,
        image = image,
        name = name,
        status = status,
        phone = phone,
        token = token,
        uid = uid,
        background = background
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
        timePosted,
        isNewPost = false,
        localPath = localPath,
        shareContentId = shareContentId,
        decentralizationType = decentralizationType,
        type = type,
        pollId = pollId
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
        timePosted,
        localPath,
        shareContentId = shareContentId,
        decentralizationType = decentralizationType,
        type = type,
        pollId = pollId
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

fun HashMap<String, Int>.toRoomEntity() : List<LikedPostEntity> {
    return this.mapNotNull { it -> LikedPostEntity(it.key, it.value) }
}

fun List<LikedPostEntity>.toDto(): HashMap<String, Int> =
    associateTo(HashMap()) { it.id to it.isLiked }

fun List<CommentEntity>.toDto() : List<CommentDTO> {
    return this.map { it -> CommentDTO(
        it.id,
        it.posterId,
        it.posterName,
        it.avatar,
        it.message,
        it.video,
        it.image,
        likeCount = it.likeCount,
        commentCount = it.commentCount,
        timePosted = it.timePosted,
        selectedNewId = it.selectedNewId
    ) }
}

fun CommentInstance.toRoomEntity(selectedNewId : String) : CommentEntity = CommentEntity(
    id,
    posterId,
    posterName,
    avatar,
    message,
    video,
    image,
    likeCount,
    commentCount,
    timePosted,
    selectedNewId
)