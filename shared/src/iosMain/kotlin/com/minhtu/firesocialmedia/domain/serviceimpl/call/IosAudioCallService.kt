package com.minhtu.firesocialmedia.domain.serviceimpl.call

import com.minhtu.firesocialmedia.data.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack

class IosAudioCallService : AudioCallService {
    override suspend fun startCallService(
        sessionId: String,
        caller: UserDTO,
        callee: UserDTO
    ) {
        // iOS implementation will be added later
    }

    override suspend fun startVideoCall(onStartVideoCall: suspend (videoTrack: WebRTCVideoTrack) -> Unit) {
        // iOS implementation will be added later
    }

    override suspend fun startVideoCallService(
        sessionId: String,
        caller: UserDTO,
        callee: UserDTO,
        currentUserId: String?,
        remoteVideoOffer: OfferAnswerDTO?
    ) {
        // iOS implementation will be added later
    }

    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (IceCandidateDTO) -> Unit,
        onRemoteVideoTrackReceived: (WebRTCVideoTrack) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun stopCall() {
        // iOS implementation will be added later
    }

    override suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        // iOS implementation will be added later
    }

    override suspend fun endCallFromApp() {
        // iOS implementation will be added later
    }

    override suspend fun rejectVideoCall() {
        // iOS implementation will be added later
    }

    override suspend fun createOffer(onOfferCreated: (offer: OfferAnswerDTO) -> Unit) {
        // iOS implementation will be added later
    }

    override suspend fun createVideoOffer(onOfferCreated: (offer: OfferAnswerDTO) -> Unit) {
        // iOS implementation will be added later
    }

    override suspend fun createAnswer(
        videoSupport: Boolean,
        onAnswerCreated: (answer: OfferAnswerDTO) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun setRemoteDescription(remoteOfferAnswer: OfferAnswerDTO) {
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
