package com.minhtu.firesocialmedia.search.entity.news

import com.minhtu.firesocialmedia.network.DecentralizationType
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance as HomeNewsInstance

fun HomeNewsInstance.toSearchNews(): NewsInstance = NewsInstance(
    id = id,
    posterId = posterId,
    posterName = posterName,
    avatar = avatar,
    message = message,
    image = image,
    video = video,
    isVisible = isVisible,
    likeCount = likeCount,
    commentCount = commentCount,
    timePosted = timePosted,
    localPath = localPath,
    shareContentId = shareContentId,
    decentralizationType = decentralizationType?.let {
        when (it) {
            is com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Public -> DecentralizationType.Public
            is com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.OnlyFriends -> DecentralizationType.OnlyFriends
            is com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Private -> DecentralizationType.Private
        }
    },
    groupId = groupId,
    type = type,
    pollId = pollId
)

fun NewsInstance.toCoreNews(): HomeNewsInstance = HomeNewsInstance(
    id = id,
    posterId = posterId,
    posterName = posterName,
    avatar = avatar,
    message = message,
    image = image,
    video = video,
    isVisible = isVisible,
    likeCount = likeCount,
    commentCount = commentCount,
    timePosted = timePosted,
    localPath = localPath,
    shareContentId = shareContentId,
    decentralizationType = decentralizationType?.let {
        when (it) {
            is DecentralizationType.Public -> com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Public
            is DecentralizationType.OnlyFriends -> com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.OnlyFriends
            is DecentralizationType.Private -> com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Private
        }
    },
    groupId = groupId,
    type = type,
    pollId = pollId
)
