package com.minhtu.firesocialmedia.notification.data.remote.mapper.news

import com.minhtu.firesocialmedia.notification.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance
import com.minhtu.firesocialmedia.notification.data.remote.dto.news.NewsDTO

fun NewsDTO.toDomain() : NewsInstance {
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
        shareContentId,
        convertDecentralizationTypeToDomain(decentralizationType),
        groupId = "",
        type = type,
        pollId = pollId
    )
}

fun convertDecentralizationTypeToDomain(decentralizationType : String) : DecentralizationType? {
    return when(decentralizationType) {
        "Public" -> DecentralizationType.Public
        "Private" -> DecentralizationType.Private
        "OnlyFriends" -> DecentralizationType.OnlyFriends
        else -> null
    }
}
