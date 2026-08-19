package com.minhtu.firesocialmedia.data.remote.mapper.settings.group
import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.domain.entity.settings.group.PollObject

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

fun PollObject.toDto(): PollDTO {
    return PollDTO(
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
