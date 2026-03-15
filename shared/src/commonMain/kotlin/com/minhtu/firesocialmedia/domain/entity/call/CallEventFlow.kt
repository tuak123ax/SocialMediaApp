package com.minhtu.firesocialmedia.domain.entity.call

import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import kotlinx.coroutines.flow.MutableStateFlow

object CallEventFlow {
    val events = MutableStateFlow<CallEvent?>(null)
    val videoCallState = MutableStateFlow<OfferAnswer?>(null)
    /** Becomes true after both sides have successfully entered video once in this call session. */
    val hasAcceptedVideoInCurrentCall = MutableStateFlow(false)
    val answerVideoCallState = MutableStateFlow(true)
    val localVideoTrack = MutableStateFlow<WebRTCVideoTrack?>(null)
    val remoteVideoTrack = MutableStateFlow<WebRTCVideoTrack?>(null)

    /** Call duration in seconds. Updated by the foreground service (notification) every second; UI reads this to stay in sync. */
    val callDurationSeconds = MutableStateFlow(0)

    /** When non-null, the other person declined the video call; UI should show this message as toast and then clear it. */
    val videoCallDeclinedMessage = MutableStateFlow<String?>(null)

    /** Reset all call state to initial values. Call when a call ends so the next call starts with a clean state. */
    fun reset() {
        events.value = null
        videoCallState.value = null
        hasAcceptedVideoInCurrentCall.value = false
        answerVideoCallState.value = true
        localVideoTrack.value = null
        remoteVideoTrack.value = null
        callDurationSeconds.value = 0
        videoCallDeclinedMessage.value = null
    }
}

sealed class CallEvent {
    object AnswerReceived : CallEvent()
    object CallEnded : CallEvent()
    object StopCalling : CallEvent()
}