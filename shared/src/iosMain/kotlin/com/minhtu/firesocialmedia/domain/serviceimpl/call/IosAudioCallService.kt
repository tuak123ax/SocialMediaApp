package com.minhtu.firesocialmedia.domain.serviceimpl.call

import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack

class IosAudioCallService : AudioCallService {
    override suspend fun startCallService(sessionId: String, caller: UserInstance, callee: UserInstance) {
        // iOS implementation will be added later
    }

    override suspend fun startVideoCall(onStartVideoCall: suspend (videoTrack: WebRTCVideoTrack) -> Unit) {
        // iOS implementation will be added later
    }

    override suspend fun startVideoCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance,
        currentUserId: String?,
        remoteVideoOffer: OfferAnswer?
    ) {
        // iOS implementation will be added later
    }

    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (iceCandidateData: IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (remoteVideoTrack: WebRTCVideoTrack) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun stopCall() {
        // iOS implementation will be added later
    }

    override fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        // iOS implementation will be added later
    }

    override fun endCallFromApp() {
        // iOS implementation will be added later
    }

    override suspend fun rejectVideoCall() {
        // iOS implementation will be added later
    }

    override suspend fun createOffer(onOfferCreated: (offer: OfferAnswer) -> Unit) {
        // iOS implementation will be added later
    }

    override suspend fun createVideoOffer(onOfferCreated: (offer: OfferAnswer) -> Unit) {
        // iOS implementation will be added later
    }

    override suspend fun createAnswer(
        videoSupport: Boolean,
        onAnswerCreated: (answer: OfferAnswer) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun setRemoteDescription(remoteOfferAnswer: OfferAnswer) {
        // iOS implementation will be added later
    }

    override suspend fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {
        // iOS implementation will be added later
    }

    override suspend fun setupAudioTrack() {
        // iOS implementation will be added later
    }

    override suspend fun releaseResources() {
        // iOS implementation will be added later
    }
}
