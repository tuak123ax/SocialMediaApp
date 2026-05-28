package com.minhtu.firesocialmedia.data.remote.mapper.settings

import com.minhtu.firesocialmedia.data.remote.dto.settings.PollDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.SessionItemDTO
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem

private const val ACTIVE_NOW_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes

fun SessionItem.toDto(): SessionItemDTO {
    return SessionItemDTO(
        sessionId = "",
        deviceName = deviceName,
        location = location,
        time = time,
        status = status
    )
}

/**
 * Maps a [SessionItemDTO] to a [SessionItem] domain entity.
 *
 * [current] is computed by comparing the stored [SessionItemDTO.sessionId] against
 * the [currentSessionId] saved locally at login time — it is never stored in the database.
 *
 * [activeNow] is derived from [SessionItemDTO.time] (epoch millis): a session is
 * considered active when the login happened within the last [ACTIVE_NOW_THRESHOLD_MS] ms —
 * it is never stored in the database.
 */
fun SessionItemDTO.toDomain(currentSessionId: String, currentTimeMillis: Long): SessionItem {
    return SessionItem(
        sessionId = sessionId,
        deviceName = deviceName,
        location = location,
        time = time,
        current = sessionId.isNotEmpty() && sessionId == currentSessionId,
        activeNow = time > 0 && (currentTimeMillis - time) < ACTIVE_NOW_THRESHOLD_MS,
        status = status
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