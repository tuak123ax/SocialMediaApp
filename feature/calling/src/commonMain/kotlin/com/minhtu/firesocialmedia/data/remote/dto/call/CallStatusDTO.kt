package com.minhtu.firesocialmedia.data.remote.dto.call

import kotlinx.serialization.Serializable

@Serializable
enum class CallStatusDTO {
    RINGING, ACCEPTED, ENDED, VIDEO
}