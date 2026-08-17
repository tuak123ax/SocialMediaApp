package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.mapper.call.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.call.toDomainCandidates
import com.minhtu.firesocialmedia.data.remote.mapper.call.toDto
import com.minhtu.firesocialmedia.calling.data.remote.mapper.user.toDto
import com.minhtu.firesocialmedia.calling.utils.Utils as CallingUtils
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.database.CallDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallStatusCallBack
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.calling.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.flow.MutableStateFlow

class CallRepositoryImpl(
    private val audioCallServiceProvider: () -> AudioCallService,
    private val callDatabaseService: CallDatabaseService,
    private val permissionManager : PermissionManager
) : CallRepository {
    private val audioCallService: AudioCallService by lazy { audioCallServiceProvider() }
    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (Any) -> Unit
    ) {
        audioCallService.initialize(
            onInitializeFinished = {
                onInitializeFinished()
            },
            onIceCandidateCreated = { iceCandidateData ->
                onIceCandidateCreated(iceCandidateData.toDomain())
            },
            onRemoteVideoTrackReceived = { remoteVideoTrack ->
                onRemoteVideoTrackReceived(remoteVideoTrack)
            }
        )
    }

    override suspend fun startCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance
    ) {
        audioCallService.startCallService(sessionId, caller.toDto(), callee.toDto())
    }

    override suspend fun startVideoCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance,
        currentUserId: String?,
        remoteVideoOffer: OfferAnswer?
    ) {
        audioCallService.startVideoCallService(
            sessionId,
            caller.toDto(),
            callee.toDto(),
            currentUserId,
            remoteVideoOffer?.toDto()
        )
    }

    override suspend fun createVideoOffer(onOfferCreated: (OfferAnswer) -> Unit) {
        audioCallService.createVideoOffer(
            onOfferCreated = { offer ->
                onOfferCreated(offer.toDomain())
            }
        )
    }

    override suspend fun createOffer(onOfferCreated: (OfferAnswer) -> Unit) {
        audioCallService.createOffer(
            onOfferCreated = { offer ->
                onOfferCreated(offer.toDomain())
            }
        )
    }

    override suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswer,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        callDatabaseService.sendOfferToFireBase(
            sessionId,
            offer.toDto(),
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                    sendIceCandidateCallBack.onSuccess()
                }
                override fun onFailure() {
                    //Send offer fail
                    sendIceCandidateCallBack.onFailure()
                }
            }
        )
    }

    override suspend fun createAnswer(
        videoSupport: Boolean,
        onAnswerCreated: (OfferAnswer) -> Unit
    ) {
        audioCallService.createAnswer(
            videoSupport,
            onAnswerCreated  = { answer ->
                onAnswerCreated(answer.toDomain())
            }
        )
    }

    override suspend fun setRemoteDescription(remoteOfferAnswer: OfferAnswer) {
        audioCallService.setRemoteDescription(remoteOfferAnswer.toDto())
    }

    override suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswer,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        callDatabaseService.sendAnswerToFirebase(
            sessionId,
            answer.toDto(),
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                    sendIceCandidateCallBack.onSuccess()
                }

                override fun onFailure() {
                    //Send offer fail
                    sendIceCandidateCallBack.onFailure()
                }

            }
        )
    }

    override suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatus) : Boolean {
        return callDatabaseService.sendCallStatusToFirebase(
            sessionId,
            status.toDto())
    }

    override suspend fun deleteCallSession(
        sessionId: String
    ) : Boolean {
        return callDatabaseService.deleteCallSession(
            sessionId
        )
    }

    override suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        audioCallService.acceptCallFromApp(sessionId, calleeId)
    }

    override suspend fun callerEndCallFromApp(currentUser : String) {
        audioCallService.callerEndCallFromApp(currentUser)
    }

    override suspend fun calleeEndCallFromApp(sessionId: String, currentUser : String) {
        audioCallService.calleeEndCallFromApp(sessionId, currentUser)
    }

    override suspend fun rejectVideoCall() {
        audioCallService.rejectVideoCall()
    }

    override suspend fun resetVideoCallStartedState() {
        audioCallService.resetVideoCallStartedState()
    }

    override suspend fun stopVideoCallResources() {
        audioCallService.stopVideoCallResources()
    }

    override suspend fun requestCameraAndAudioPermissions(): Boolean {
        return permissionManager.requestCameraAndAudioPermissions()
    }

    override suspend fun requestAudioPermission(): Boolean {
        return permissionManager.requestAudioPermission()
    }

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSession,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        callDatabaseService.sendCallSessionToFirebase(
            session.toDto(),
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send call session success
                    sendCallSessionCallBack.onSuccess()
                }

                override fun onFailure() {
                    //Send call session fail
                    logMessage("sendCallSessionToFirebase", {"send call session fail"})
                    sendCallSessionCallBack.onFailure()
                }

            })
    }

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (IceCandidateData) -> Unit
    ) {
        callDatabaseService.observeIceCandidatesFromCallee(
            sessionId,
            iceCandidateCallBack = { iceCandidate ->
                iceCandidateCallBack(iceCandidate.toDomain())
            })
    }

    override suspend fun startVideoCall(
        isVideoInitiator: Boolean,
        onStartVideoCall: suspend (Any) -> Unit
    ) {
        audioCallService.startVideoCall(
            isVideoInitiator = isVideoInitiator,
            onStartVideoCall = { localVideoTrack ->
                onStartVideoCall(localVideoTrack)
            }
        )
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (OfferAnswer) -> Unit
    ) {
        callDatabaseService.observeVideoCall(
            sessionId,
            videoCallCallBack = { videoOffer ->
                videoCallCallBack(videoOffer.toDomain())
            }
        )
    }

    override suspend fun addIceCandidate(
        sdp: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ) {
        audioCallService.addIceCandidate(sdp, sdpMid, sdpMLineIndex)
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (OfferAnswer) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        callDatabaseService.observeAnswerFromCallee(
            sessionId,
            answerCallBack = { remoteAnswer ->
                answerCallBack(remoteAnswer.toDomain())
            },
            rejectCallBack = {
                rejectCallBack()
            }
        )
    }

    override suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        callDatabaseService.updateOfferInFirebase(
            sessionId,
            "",
            "initiator",
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                }

                override fun onFailure() {
                    //Send offer fail
                }

            }
        )
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: CallStatusCallBack
    ) {
        callDatabaseService.observeCallStatus(
            sessionId,
            object : CallingUtils.Companion.CallStatusCallBack{
                override fun onSuccess(status : com.minhtu.firesocialmedia.data.remote.dto.call.CallStatusDTO) {
                    callStatusCallBack.onSuccess(status.toDomain())
                }

                override fun onFailure() {
                    callStatusCallBack.onFailure()
                }
            }
        )
    }

    override suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        callDatabaseService.updateAnswerInFirebase(
            sessionId,
            "Reject",
            "initiator",
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                    updateAnswerCallBack.onSuccess()
                }

                override fun onFailure() {
                    //Send offer fail
                    updateAnswerCallBack.onFailure()
                }

            }
        )
    }

    override suspend fun clearAnswerInFirebase(sessionId: String) {
        callDatabaseService.clearAnswerInFirebase(sessionId)
        logMessage("clearAnswerInFirebase", { "clear Answer completed" })
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        callDatabaseService.observePhoneCallWithoutCheckingInCall(
            currentUserId,
            phoneCallCallBack = { callingRequestDTO ->
                phoneCallCallBack(callingRequestDTO.toDomain())
            },
            endCallSession = { end ->
                endCallSession(end)
            },
            whoEndCallCallBack = { whoEndCall ->
                whoEndCallCallBack(whoEndCall)
            },
            iceCandidateCallBack = { iceCandidates ->
                //Add ice candidates to peer connection.
                iceCandidateCallBack(iceCandidates.toDomainCandidates())
            })
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        callDatabaseService.observePhoneCall(
            isInCall,
            currentUserId,
            phoneCallCallBack = { callingRequestDTO ->
                phoneCallCallBack(callingRequestDTO.toDomain())
            },
            whoEndCallCallBack = { whoEndCall ->
                whoEndCallCallBack(whoEndCall)
            },
            endCallSession = { end ->
                endCallSession(end)
            },
            iceCandidateCallBack = { iceCandidates ->
                iceCandidateCallBack(iceCandidates.toDomainCandidates())
            })
    }

    override fun stopObservePhoneCall() {
        callDatabaseService.stopObservePhoneCall()
    }

    override suspend fun updateMuteStatus(muted: Boolean) {
        audioCallService.updateMuteStatus(muted)
    }

    override suspend fun updateCameraStatus(cameraOff: Boolean) {
        audioCallService.updateCameraStatus(cameraOff)
    }

    override suspend fun updateSpeakerStatus(speakerType: SpeakerType) {
        audioCallService.updateSpeakerStatus(speakerType)
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateData,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        callDatabaseService.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate.toDto(),
            whichCandidate,
            sendIceCandidateCallBack)
    }

    override suspend fun sendWhoEndCall(
        sessionId: String,
        whoEndCall: String
    ): Boolean {
        return callDatabaseService.sendWhoEndCall(
            sessionId,
            whoEndCall
        )
    }

    override suspend fun stopCallService() {
        audioCallService.stopCall()
    }
}
