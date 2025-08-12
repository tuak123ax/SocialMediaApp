package com.minhtu.firesocialmedia.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class OfferAnswer(
    var sdp: String? = null,
    var type: String? = null,
    var initiator: String = ""
)