package com.minhtu.firesocialmedia.data.remote.dto.call

import kotlinx.serialization.Serializable

@Serializable
data class OfferAnswerDTO(
    var sdp: String? = null,
    var type: String? = null,
    var initiator: String = ""
)