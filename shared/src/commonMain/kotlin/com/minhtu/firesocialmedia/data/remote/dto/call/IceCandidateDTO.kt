package com.minhtu.firesocialmedia.data.remote.dto.call

import kotlinx.serialization.Serializable

@Serializable
data class IceCandidateDTO(
    var candidate: String? = null,
    var sdpMid: String? = null,
    var sdpMLineIndex: Int? = null
)