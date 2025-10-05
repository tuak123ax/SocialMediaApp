package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.constant.DataConstant
import com.minhtu.firesocialmedia.data.mapper.call.toDomain
import com.minhtu.firesocialmedia.data.mapper.call.toDomainCandidates
import com.minhtu.firesocialmedia.data.mapper.call.toDto
import com.minhtu.firesocialmedia.data.mapper.user.toDto
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.data.remote.service.permission.PermissionManager
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow

class CallRepositoryImpl(
    private val audioCallService: AudioCallService,
    private val databaseService: DatabaseService,
    private val callService: AudioCallService,
    private val permissionManager : PermissionManager
) : CallRepository {
    override suspend fun initialize(
        onInitializeFinished: () -> Unit,
        onIceCandidateCreated: (IceCandidateData) -> Unit,
        onRemoteVideoTrackReceived: (WebRTCVideoTrack) -> Unit
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

    override suspend fun isCalleeInActiveCall(calleeId: String) : Boolean? {
        return databaseService.isCalleeInActiveCall(calleeId, DataConstant.CALL_PATH)
    }

    override suspend fun startCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance
    ) {
        callService.startCallService(sessionId, caller.toDto(), callee.toDto())
    }

    override suspend fun startVideoCallService(
        sessionId: String,
        caller: UserInstance,
        callee: UserInstance,
        currentUserId: String?,
        remoteVideoOffer: OfferAnswer?
    ) {
        callService.startVideoCallService(
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
        databaseService.sendOfferToFireBase(
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
        databaseService.sendAnswerToFirebase(
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
        return databaseService.sendCallStatusToFirebase(
            sessionId,
            status)
    }

    override suspend fun deleteCallSession(
        sessionId: String
    ) : Boolean {
        return databaseService.deleteCallSession(
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
        databaseService.sendCallSessionToFirebase(
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
        databaseService.observeIceCandidatesFromCallee(
            sessionId,
            iceCandidateCallBack = { iceCandidate ->
                iceCandidateCallBack(iceCandidate.toDomain())
            })
    }

    override suspend fun startVideoCall(onStartVideoCall: suspend (WebRTCVideoTrack) -> Unit) {
        audioCallService.startVideoCall(
            onStartVideoCall = { localVideoTrack ->
                onStartVideoCall(localVideoTrack)
            }
        )
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (OfferAnswer) -> Unit
    ) {
        databaseService.observeVideoCall(
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
        databaseService.observeAnswerFromCallee(
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
        databaseService.updateOfferInFirebase(
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
        callStatusCallBack: Utils.Companion.CallStatusCallBack
    ) {
        databaseService.observeCallStatus(
            sessionId,
            object : Utils.Companion.CallStatusCallBack{
                override fun onSuccess(status : CallStatus) {
                    callStatusCallBack.onSuccess(status)
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
        databaseService.updateAnswerInFirebase(
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

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestData) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        databaseService.observePhoneCallWithoutCheckingInCall(
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
        databaseService.observePhoneCall(
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
        databaseService.stopObservePhoneCall()
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateData,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        databaseService.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate.toDto(),
            whichCandidate,
            sendIceCandidateCallBack)
    }

    override suspend fun sendWhoEndCall(
        sessionId: String,
        whoEndCall: String
    ): Boolean {
        return databaseService.sendWhoEndCall(
            sessionId,
            whoEndCall
        )
    }

    override suspend fun stopCallService() {
        callService.stopCall()
    }
}