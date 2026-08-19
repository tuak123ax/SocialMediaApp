package com.minhtu.firesocialmedia.testutil

import com.minhtu.firesocialmedia.calling.entity.user.UserInstance
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallStatusCallBack
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Hand-rolled fake implementation of [CallRepository] used across the calling module's tests.
 * Every method records what it was called with, and behavior is configurable via the constructor
 * / public vars so individual tests can drive callbacks the way the real Firebase/WebRTC backed
 * implementation would.
 */
class FakeCallRepository(
    var createVideoOfferResult: OfferAnswer = OfferAnswer(sdp = "video-sdp", type = "offer", initiator = "caller"),
    var createOfferResult: OfferAnswer = OfferAnswer(sdp = "offer-sdp", type = "offer", initiator = "caller"),
    var sendOfferToFireBaseSuccess: Boolean = true,
    var createAnswerResult: OfferAnswer = OfferAnswer(sdp = "answer-sdp", type = "answer", initiator = "callee"),
    var sendAnswerToFirebaseSuccess: Boolean = true,
    var sendCallStatusToFirebaseResult: Boolean = true,
    var deleteCallSessionResult: Boolean = true,
    var requestCameraAndAudioPermissionsResult: Boolean = true,
    var requestAudioPermissionResult: Boolean = true,
    var sendCallSessionToFirebaseSuccess: Boolean = true,
    var updateOfferInFirebaseSuccess: Boolean = true,
    var updateAnswerInFirebaseSuccess: Boolean = true,
    var sendIceCandidateToFireBaseSuccess: Boolean = true,
    var sendWhoEndCallResult: Boolean = true,
    var initializeShouldThrow: Boolean = false,
    var phoneCallRequestToEmit: CallingRequestData? = null,
    var endCallSessionToEmit: Boolean? = null,
    var iceCandidatesToEmitOnObserve: List<IceCandidateData> = emptyList(),
    var videoOfferToEmitOnObserve: OfferAnswer? = null,
    var answerToEmitOnObserve: OfferAnswer? = null,
    var shouldInvokeRejectCallback: Boolean = false,
    var callStatusToEmit: CallStatus? = null,
    var shouldInvokeCallStatusFailure: Boolean = false,
    var localVideoTrackToEmit: Any? = "fake-local-video-track"
) : CallRepository {

    // ── initialize ──
    var initializeCalled = false
    var iceCandidateCreatedCallback: ((IceCandidateData) -> Unit)? = null
    var remoteVideoTrackReceivedCallback: ((Any) -> Unit)? = null
    var initializeShouldInvokeFinished = true

    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (Any) -> Unit
    ) {
        initializeCalled = true
        iceCandidateCreatedCallback = onIceCandidateCreated
        remoteVideoTrackReceivedCallback = onRemoteVideoTrackReceived
        if (initializeShouldThrow) throw RuntimeException("fake initialize failure")
        if (initializeShouldInvokeFinished) onInitializeFinished()
    }

    // ── call service ──
    var startCallServiceCalledWith: Triple<String, UserInstance, UserInstance>? = null
    override suspend fun startCallService(sessionId: String, caller: UserInstance, callee: UserInstance) {
        startCallServiceCalledWith = Triple(sessionId, caller, callee)
    }

    data class StartVideoCallServiceArgs(
        val sessionId: String, val caller: UserInstance, val callee: UserInstance,
        val currentUserId: String?, val remoteVideoOffer: OfferAnswer?
    )
    var startVideoCallServiceCalledWith: StartVideoCallServiceArgs? = null
    override suspend fun startVideoCallService(
        sessionId: String, caller: UserInstance, callee: UserInstance,
        currentUserId: String?, remoteVideoOffer: OfferAnswer?
    ) {
        startVideoCallServiceCalledWith = StartVideoCallServiceArgs(sessionId, caller, callee, currentUserId, remoteVideoOffer)
    }

    // ── offer / answer ──
    var createVideoOfferCalled = false
    override suspend fun createVideoOffer(onOfferCreated: (OfferAnswer) -> Unit) {
        createVideoOfferCalled = true
        onOfferCreated(createVideoOfferResult)
    }

    var createOfferCalled = false
    override suspend fun createOffer(onOfferCreated: (OfferAnswer) -> Unit) {
        createOfferCalled = true
        onOfferCreated(createOfferResult)
    }

    var sendOfferToFireBaseCalledWith: Pair<String, OfferAnswer>? = null
    override suspend fun sendOfferToFireBase(
        sessionId: String, offer: OfferAnswer,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        sendOfferToFireBaseCalledWith = sessionId to offer
        if (sendOfferToFireBaseSuccess) sendIceCandidateCallBack.onSuccess() else sendIceCandidateCallBack.onFailure()
    }

    var createAnswerCalledWithVideoSupport: Boolean? = null
    override suspend fun createAnswer(videoSupport: Boolean, onAnswerCreated: (OfferAnswer) -> Unit) {
        createAnswerCalledWithVideoSupport = videoSupport
        onAnswerCreated(createAnswerResult)
    }

    var setRemoteDescriptionCalledWith: OfferAnswer? = null
    override suspend fun setRemoteDescription(remoteOfferAnswer: OfferAnswer) {
        setRemoteDescriptionCalledWith = remoteOfferAnswer
    }

    var sendAnswerToFirebaseCalledWith: Pair<String, OfferAnswer>? = null
    override suspend fun sendAnswerToFirebase(
        sessionId: String, answer: OfferAnswer,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        sendAnswerToFirebaseCalledWith = sessionId to answer
        if (sendAnswerToFirebaseSuccess) sendIceCandidateCallBack.onSuccess() else sendIceCandidateCallBack.onFailure()
    }

    // ── call status / lifecycle ──
    var sendCallStatusToFirebaseCalledWith: Pair<String, CallStatus>? = null
    override suspend fun sendCallStatusToFirebase(sessionId: String, status: CallStatus): Boolean {
        sendCallStatusToFirebaseCalledWith = sessionId to status
        return sendCallStatusToFirebaseResult
    }

    var deleteCallSessionCalledWith: String? = null
    override suspend fun deleteCallSession(sessionId: String): Boolean {
        deleteCallSessionCalledWith = sessionId
        return deleteCallSessionResult
    }

    var acceptCallFromAppCalledWith: Pair<String, String?>? = null
    override suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        acceptCallFromAppCalledWith = sessionId to calleeId
    }

    var callerEndCallInvokedWith: String? = null
    override suspend fun callerEndCallFromApp(currentUser: String) {
        callerEndCallInvokedWith = currentUser
    }

    var calleeEndCallInvokedWith: Pair<String, String>? = null
    override suspend fun calleeEndCallFromApp(sessionId: String, currentUser: String) {
        calleeEndCallInvokedWith = sessionId to currentUser
    }

    var rejectVideoCallInvoked = false
    override suspend fun rejectVideoCall() {
        rejectVideoCallInvoked = true
    }

    var resetVideoCallStartedStateInvoked = false
    override suspend fun resetVideoCallStartedState() {
        resetVideoCallStartedStateInvoked = true
    }

    var stopVideoCallResourcesInvoked = false
    override suspend fun stopVideoCallResources() {
        stopVideoCallResourcesInvoked = true
    }

    // ── permissions ──
    override suspend fun requestCameraAndAudioPermissions(): Boolean = requestCameraAndAudioPermissionsResult
    override suspend fun requestAudioPermission(): Boolean = requestAudioPermissionResult

    // ── signaling ──
    var sendCallSessionToFirebaseCalledWith: AudioCallSession? = null
    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSession,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        sendCallSessionToFirebaseCalledWith = session
        if (sendCallSessionToFirebaseSuccess) sendCallSessionCallBack.onSuccess() else sendCallSessionCallBack.onFailure()
    }

    var observeIceCandidatesFromCalleeCalledWith: String? = null
    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (IceCandidateData) -> Unit
    ) {
        observeIceCandidatesFromCalleeCalledWith = sessionId
        iceCandidatesToEmitOnObserve.forEach { iceCandidateCallBack(it) }
    }

    var startVideoCallCalledWithInitiator: Boolean? = null
    override suspend fun startVideoCall(isVideoInitiator: Boolean, onStartVideoCall: suspend (Any) -> Unit) {
        startVideoCallCalledWithInitiator = isVideoInitiator
        localVideoTrackToEmit?.let { onStartVideoCall(it) }
    }

    var observeVideoCallCalledWith: String? = null
    override suspend fun observeVideoCall(sessionId: String, videoCallCallBack: (OfferAnswer) -> Unit) {
        observeVideoCallCalledWith = sessionId
        videoOfferToEmitOnObserve?.let { videoCallCallBack(it) }
    }

    data class IceCandidateArgs(val sdp: String, val sdpMid: String, val sdpMLineIndex: Int)
    val addIceCandidateCalls = mutableListOf<IceCandidateArgs>()
    override suspend fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {
        addIceCandidateCalls.add(IceCandidateArgs(sdp, sdpMid, sdpMLineIndex))
    }

    var observeAnswerFromCalleeCalledWith: String? = null
    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (OfferAnswer) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        observeAnswerFromCalleeCalledWith = sessionId
        answerToEmitOnObserve?.let { answerCallBack(it) }
        if (shouldInvokeRejectCallback) rejectCallBack()
    }

    var updateOfferInFirebaseCalledWith: Triple<String, String, String>? = null
    override suspend fun updateOfferInFirebase(
        sessionId: String, updateContent: String,
        updateField: String, updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        updateOfferInFirebaseCalledWith = Triple(sessionId, updateContent, updateField)
        if (updateOfferInFirebaseSuccess) updateOfferCallBack.onSuccess() else updateOfferCallBack.onFailure()
    }

    var observeCallStatusCalledWith: String? = null
    override suspend fun observeCallStatus(sessionId: String, callStatusCallBack: CallStatusCallBack) {
        observeCallStatusCalledWith = sessionId
        callStatusToEmit?.let { callStatusCallBack.onSuccess(it) }
        if (shouldInvokeCallStatusFailure) callStatusCallBack.onFailure()
    }

    var updateAnswerInFirebaseCalledWith: Triple<String, String, String>? = null
    override suspend fun updateAnswerInFirebase(
        sessionId: String, updateContent: String,
        updateField: String, updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        updateAnswerInFirebaseCalledWith = Triple(sessionId, updateContent, updateField)
        if (updateAnswerInFirebaseSuccess) updateAnswerCallBack.onSuccess() else updateAnswerCallBack.onFailure()
    }

    var clearAnswerInFirebaseCalledWith: String? = null
    override suspend fun clearAnswerInFirebase(sessionId: String) {
        clearAnswerInFirebaseCalledWith = sessionId
    }

    var observePhoneCallWithoutCheckingInCallCalledWith: String? = null
    var whoEndCallToEmit: String? = null
    var iceCandidatesMapToEmit: Map<String, IceCandidateData>? = null
    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        observePhoneCallWithoutCheckingInCallCalledWith = currentUserId
        phoneCallRequestToEmit?.let { phoneCallCallBack(it) }
        endCallSessionToEmit?.let { endCallSession(it) }
        whoEndCallToEmit?.let { whoEndCallCallBack(it) }
        if (iceCandidatesMapToEmit != null) iceCandidateCallBack(iceCandidatesMapToEmit)
    }

    var observePhoneCallCalledWith: String? = null
    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        observePhoneCallCalledWith = currentUserId
        phoneCallRequestToEmit?.let { phoneCallCallBack(it) }
        endCallSessionToEmit?.let { endCallSession(it) }
        whoEndCallToEmit?.let { whoEndCallCallBack(it) }
        if (iceCandidatesMapToEmit != null) iceCandidateCallBack(iceCandidatesMapToEmit)
    }

    var sendIceCandidateToFireBaseCalledWith: Triple<String, IceCandidateData, String>? = null
    override suspend fun sendIceCandidateToFireBase(
        sessionId: String, iceCandidate: IceCandidateData,
        whichCandidate: String, sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        sendIceCandidateToFireBaseCalledWith = Triple(sessionId, iceCandidate, whichCandidate)
        if (sendIceCandidateToFireBaseSuccess) sendIceCandidateCallBack.onSuccess() else sendIceCandidateCallBack.onFailure()
    }

    var sendWhoEndCallCalledWith: Pair<String, String>? = null
    override suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean {
        sendWhoEndCallCalledWith = sessionId to whoEndCall
        return sendWhoEndCallResult
    }

    var stopCallServiceInvoked = false
    override suspend fun stopCallService() {
        stopCallServiceInvoked = true
    }

    var stopObservePhoneCallInvoked = false
    override fun stopObservePhoneCall() {
        stopObservePhoneCallInvoked = true
    }

    var muteStatus: Boolean? = null
    override suspend fun updateMuteStatus(muted: Boolean) {
        muteStatus = muted
    }

    var cameraStatus: Boolean? = null
    override suspend fun updateCameraStatus(cameraOff: Boolean) {
        cameraStatus = cameraOff
    }

    var speakerStatus: SpeakerType? = null
    override suspend fun updateSpeakerStatus(speakerType: SpeakerType) {
        speakerStatus = speakerType
    }
}
