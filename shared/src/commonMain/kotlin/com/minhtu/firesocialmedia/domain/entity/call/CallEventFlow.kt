package com.minhtu.firesocialmedia.domain.entity.call

import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import kotlinx.coroutines.flow.MutableStateFlow

object CallEventFlow {
    val events = MutableStateFlow<CallEvent?>(null)
    val videoCallState = MutableStateFlow<OfferAnswer?>(null)
    val answerVideoCallState = MutableStateFlow(true)
    val localVideoTrack = MutableStateFlow<WebRTCVideoTrack?>(null)
    val remoteVideoTrack = MutableStateFlow<WebRTCVideoTrack?>(null)
}

sealed class CallEvent {
    object AnswerReceived : CallEvent()
    object CallEnded : CallEvent()
    object StopCalling : CallEvent()
}