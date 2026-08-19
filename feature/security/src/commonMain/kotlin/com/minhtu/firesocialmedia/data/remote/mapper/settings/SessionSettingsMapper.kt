package com.minhtu.firesocialmedia.data.remote.mapper.settings

import com.minhtu.firesocialmedia.data.remote.dto.settings.security.SessionItemDTO
import com.minhtu.firesocialmedia.domain.entity.settings.SessionItem

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
