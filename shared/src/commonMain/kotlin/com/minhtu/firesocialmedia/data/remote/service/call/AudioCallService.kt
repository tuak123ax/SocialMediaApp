package com.minhtu.firesocialmedia.data.remote.service.call

import com.minhtu.firesocialmedia.data.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.dto.user.UserDTO
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack

interface AudioCallService{
    /**
     * This function is used to start call foreground service.
     * @Param:
     * sessionId: id of call session.
     * caller: information of caller.
     * callee: information of callee.
     * */
    suspend fun startCallService(sessionId : String, caller : UserDTO, callee : UserDTO)

    /**
     * This function is used to start video call.
     * @Param:
     * onStartVideoCall: return local video track when it is available.
     * */
    suspend fun startVideoCall(onStartVideoCall : suspend (videoTrack : WebRTCVideoTrack) -> Unit)

    /**
     * This function is used to start video call foreground service.
     * @Param:
     * sessionId: id of call session.
     * caller: information of caller.
     * callee: information of callee.
     * currentUserId: id of current user.
     * remoteVideoOffer: offer from caller if have.
     * */
    suspend fun startVideoCallService(sessionId : String,
                                      caller : UserDTO,
                                      callee : UserDTO,
                                      currentUserId : String?,
                                      remoteVideoOffer : OfferAnswerDTO?)

    /**
     * This function is used to setup servers, peer connection and audio.
     * @Param:
     * onIceCandidateCreated: return ice candidate when it is initialized.
     * onRemoteVideoTrackReceived: return remote video track to show on screen.
     * */
    suspend fun initialize(onInitializeFinished : () -> Unit,
                           onIceCandidateCreated : (iceCandidateData : IceCandidateDTO) -> Unit,
                           onRemoteVideoTrackReceived: (remoteVideoTrack : WebRTCVideoTrack) -> Unit)

    /**
     * This function is used to stop call foreground service.
     * */
    suspend fun stopCall()

    /**
     * This function is called when user accept call from app instead of notification.
     * @Param:
     * sessionId: session id of the call.
     * calleeId: id of callee.
     * */
    suspend fun acceptCallFromApp(sessionId: String, calleeId: String?)

    /**
     * This function is called when user end call from app instead of notification.
     * */
    suspend fun callerEndCallFromApp(currentUser : String)

    /**
     * This function is called when user end call from app instead of notification.
     * */
    suspend fun calleeEndCallFromApp(sessionId: String, currentUser : String)

    /**
     * This function is used to reject video call.
     * */
    suspend fun rejectVideoCall()

    /**
     * This function is used to create audio offer for caller.
     * @Param:
     * onOfferCreated: return created audio offer to process next step.
     * */
    suspend fun createOffer(onOfferCreated : (offer : OfferAnswerDTO) -> Unit)

    /**
     * This function is used to create video offer for caller.
     * @Param:
     * onOfferCreated: return created video offer to process next step.
     * */
    suspend fun createVideoOffer(onOfferCreated : (offer : OfferAnswerDTO) -> Unit)

    /**
     * This function is used to create answer for callee.
     * @Param:
     * videoSupport: flag to know to create answer for audio call or video call.
     * onAnswerCreated: return created answer to process next step.
     * */
    suspend fun createAnswer(videoSupport : Boolean, onAnswerCreated : (answer : OfferAnswerDTO) -> Unit)

    /**
     * This function is used to set remote description when receive from other user.
     * @Param:
     * remoteOffer: remote offer/answer to set in remote description of peer connection.
     * */
    suspend fun setRemoteDescription(remoteOfferAnswer : OfferAnswerDTO)

    /**
     * This function is used to add ice candidate to peer connection.
     * */
    suspend fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int)

    /**
     * This function is used to setup audio track.
     * */
    suspend fun setupAudioTrack()

    /**
     * This function is used to release all resources of foreground service.
     * */
    suspend fun releaseResources()
}