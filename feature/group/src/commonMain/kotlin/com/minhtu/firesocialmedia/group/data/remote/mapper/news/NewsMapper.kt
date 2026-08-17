package com.minhtu.firesocialmedia.group.data.remote.mapper.news

import com.minhtu.firesocialmedia.group.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO

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
fun NewsInstance.toDto() : NewsDTO {
    return NewsDTO(
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
        decentralizationType?.toString() ?: "",
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
