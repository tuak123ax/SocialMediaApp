package com.minhtu.firesocialmedia.data.mapper.news

import com.minhtu.firesocialmedia.data.dto.news.NewsDTO
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
        timePosted
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
        timePosted
    )
}