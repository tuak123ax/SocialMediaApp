package com.minhtu.firesocialmedia.home.entity.call

data class CallingRequestData(
    val sessionId: String = "",
    val callerId: String = "",
    val calleeId: String = "",
    val offer: OfferAnswer? = null
)
