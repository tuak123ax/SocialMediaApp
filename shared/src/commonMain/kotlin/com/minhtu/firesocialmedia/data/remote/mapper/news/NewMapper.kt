package com.minhtu.firesocialmedia.data.remote.mapper.news

import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

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
        localPath
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
        localPath
    )
}