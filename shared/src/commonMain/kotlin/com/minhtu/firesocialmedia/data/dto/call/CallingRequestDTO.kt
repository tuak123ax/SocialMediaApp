package com.minhtu.firesocialmedia.data.dto.call

data class CallingRequestDTO(
    val sessionId : String = "",
    val callerId : String = "",
    val calleeId : String = "",
    val offer : OfferAnswerDTO? = null
)