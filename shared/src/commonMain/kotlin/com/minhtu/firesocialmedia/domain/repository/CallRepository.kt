package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow

interface CallRepository {
    suspend fun initialize(
        onInitializeFinished : () -> Unit,
        onIceCandidateCreated : (iceCandidateData : IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (remoteVideoTrack : WebRTCVideoTrack) -> Unit
    )
    suspend fun isCalleeInActiveCall(
        calleeId: String
    ) : Boolean?

    suspend fun startCallService(
        sessionId : String,
        caller : UserInstance,
        callee : UserInstance
    )

    suspend fun startVideoCallService(
        sessionId : String,
        caller : UserInstance,
        callee : UserInstance,
        currentUserId : String?,
        remoteVideoOffer : OfferAnswer?
    )

    suspend fun createVideoOffer(onOfferCreated : (offer : OfferAnswer) -> Unit)

    suspend fun createOffer(onOfferCreated : (offer : OfferAnswer) -> Unit)

    suspend fun sendOfferToFireBase(
        sessionId : String,
        offer: OfferAnswer,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun createAnswer(videoSupport : Boolean, onAnswerCreated : (answer : OfferAnswer) -> Unit)
    suspend fun setRemoteDescription(remoteOfferAnswer : OfferAnswer)
    suspend fun sendAnswerToFirebase(
        sessionId : String,
        answer: OfferAnswer,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun sendCallStatusToFirebase(sessionId: String,
                                 status: CallStatus) : Boolean

    suspend fun deleteCallSession(
        sessionId: String
    ) : Boolean

    suspend fun acceptCallFromApp(sessionId: String, calleeId: String?)

    suspend fun callerEndCallFromApp(currentUser : String)
    suspend fun calleeEndCallFromApp(sessionId: String, currentUser : String)

    suspend fun rejectVideoCall()

    suspend fun requestCameraAndAudioPermissions(): Boolean

    suspend fun requestAudioPermission(): Boolean

    suspend fun sendCallSessionToFirebase(session: AudioCallSession,
                                          sendCallSessionCallBack : Utils.Companion.BasicCallBack)

    suspend fun observeIceCandidatesFromCallee(
        sessionId : String,
        iceCandidateCallBack: (iceCandidate : IceCandidateData) -> Unit
    )

    suspend fun startVideoCall(
        onStartVideoCall : suspend (videoTrack : WebRTCVideoTrack) -> Unit)

    suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer : OfferAnswer) -> Unit
    )

    suspend fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int)

    suspend fun observeAnswerFromCallee(
        sessionId : String,
        answerCallBack : (answer : OfferAnswer) -> Unit,
        rejectCallBack : () -> Unit
    )

    suspend fun updateOfferInFirebase(
        sessionId : String,
        updateContent: String,
        updateField : String,
        updateOfferCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun observeCallStatus(
        sessionId : String,
        callStatusCallBack : Utils.Companion.CallStatusCallBack
    )

    suspend fun updateAnswerInFirebase(
        sessionId : String,
        updateContent: String,
        updateField : String,
        updateAnswerCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId : String,
        phoneCallCallBack : (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit)

    suspend fun observePhoneCall(
        isInCall : MutableStateFlow<Boolean>,
        currentUserId : String,
        phoneCallCallBack : (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit)

    suspend fun sendIceCandidateToFireBase(sessionId : String,
                                           iceCandidate: IceCandidateData,
                                           whichCandidate : String,
                                           sendIceCandidateCallBack : Utils.Companion.BasicCallBack)

    suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean
    suspend fun stopCallService()
    fun stopObservePhoneCall()
}