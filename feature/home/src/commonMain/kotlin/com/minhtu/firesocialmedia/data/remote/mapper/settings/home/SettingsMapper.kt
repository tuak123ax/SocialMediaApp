package com.minhtu.firesocialmedia.data.remote.mapper.settings.home
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.domain.entity.settings.home.PollObject

fun PollDTO.toDomain(): PollObject {
    return PollObject(
        id = id,
        posterId = posterId,
        posterName = posterName,
        posterAvatar = posterAvatar,
        question = question,
        options = options,
        allowMultipleAnswers = allowMultipleAnswers,
        duration = duration,
        groupId = groupId,
        likeCount = likeCount,
        commentCount = commentCount,
        timePosted = timePosted,
        expiresAt = expiresAt,
        votes = votes,
    )
}
