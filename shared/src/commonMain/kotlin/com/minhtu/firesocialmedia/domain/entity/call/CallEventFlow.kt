package com.minhtu.firesocialmedia.domain.entity.call

import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import kotlinx.coroutines.flow.MutableStateFlow

object CallEventFlow {
    val events = MutableStateFlow<CallEvent?>(null)
    val videoCallState = MutableStateFlow<OfferAnswer?>(null)
    val answerVideoCallState = MutableStateFlow(true)
    val localVideoTrack = MutableStateFlow<WebRTCVideoTrack?>(null)
    val remoteVideoTrack = MutableStateFlow<WebRTCVideoTrack?>(null)

    /** Call duration in seconds. Updated by the foreground service (notification) every second; UI reads this to stay in sync. */
    val callDurationSeconds = MutableStateFlow(0)

    /** Reset all call state to initial values. Call when a call ends so the next call starts with a clean state. */
    fun reset() {
        events.value = null
        videoCallState.value = null
        answerVideoCallState.value = true
        localVideoTrack.value = null
        remoteVideoTrack.value = null
        callDurationSeconds.value = 0
    }
}

sealed class CallEvent {
    object AnswerReceived : CallEvent()
    object CallEnded : CallEvent()
    object StopCalling : CallEvent()
}