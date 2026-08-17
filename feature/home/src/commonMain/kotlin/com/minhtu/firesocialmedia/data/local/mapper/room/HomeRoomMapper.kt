package com.minhtu.firesocialmedia.data.local.mapper.room

import com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance

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

fun HashMap<String, Int>.toRoomEntity() : List<LikedPostEntity> {
    return this.mapNotNull { it -> LikedPostEntity(it.key, it.value) }
}

fun List<LikedPostEntity>.toDto(): HashMap<String, Int> =
    associateTo(HashMap()) { it.id to it.isLiked }
