package com.minhtu.firesocialmedia.data.remote.dto.settings

import kotlinx.serialization.Serializable

@Serializable
data class SessionItemDTO(
    val sessionId: String = "",
    val deviceName: String = "",
    val location: String = "",
    val time: Long = 0L
)