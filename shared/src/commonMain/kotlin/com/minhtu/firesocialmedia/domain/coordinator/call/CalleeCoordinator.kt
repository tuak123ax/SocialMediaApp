package com.minhtu.firesocialmedia.domain.coordinator.call

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.usecases.call.CalleeUseCases
import com.minhtu.firesocialmedia.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.getCallTypeFromSdp

class CalleeCoordinator(
    val calleeUseCases: CalleeUseCases,
    val initializeCallUseCase: InitializeCallUseCase,
    val videoCallUseCase : VideoCallUseCase
) {
    suspend fun startCall(
        sessionId : String,
        calleeId : String,
        onReceivePhoneCallRequest :  suspend (CallingRequestData) -> Unit,
        onEndCall : suspend () -> Unit,
        whoEndCallCallBack : suspend (String) -> Unit
    ) {
        //Callee starts call
        calleeUseCases.listenForIncomingCalls.invoke(
            onInitializeFinished = {
                logMessage("observePhoneCallWithoutCheckingInCall", { "start observe" })
                //Observe phone call request.
                calleeUseCases.observePhoneCall.invoke(
                    calleeId,
                    onReceivePhoneCallRequest = { callingRequestData->
                        onReceivePhoneCallRequest(callingRequestData)
                    },
                    iceCandidateCallBack = { iceCandidates ->
                        // Keep this callback limited to ICE delivery. Reapplying the original
                        // audio offer here during video upgrade can overwrite the fresh video offer.
                        if(iceCandidates != null) {
                            logMessage("startCall" , { "addIceCandidates" })
                            calleeUseCases.addIceCandidates.invoke(iceCandidates)
                        }
                    },
                    onEndCall = {
                        val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
                        if(deleteCallSessionResult) {
                            onEndCall()
                        }
                    },
                    whoEndCallCallBack = { whoEndCall ->
                        whoEndCallCallBack(whoEndCall)
                    }
                )
            },
            onIceCandidateCreated = { iceCandidateData ->
                //Send ice candidate to DB after created
                calleeUseCases.sendIceCandidate.invoke(
                    sessionId,
                    iceCandidateData,
                    "calleeCandidates"
                )
            }
        )
    }

    suspend fun acceptCall(
        sessionId : String,
        calleeId : String,
        offer: OfferAnswer,
        onAcceptCall : suspend (Boolean) -> Unit,
        onReceiveVideoCallRequest : suspend (OfferAnswer) -> Unit) {
        // Ensure remote description is set before creating the answer
        initializeCallUseCase.setRemoteDescription(offer)

        // Callee send answer
        calleeUseCases.sendAnswer.invoke(
            sessionId,
            offer,
            onSendAnswerResult = { result ->
                if(result) {
                    logMessage("onSendAnswerResult", {"success"})
                } else {
                    logMessage("onSendAnswerResult", {"fail"})
                }
            }
        )
        //Accept call
        val sendAcceptCallStatus = calleeUseCases.acceptCall.invoke(sessionId)
        if(sendAcceptCallStatus) {
            //Start observe video call for callee
            logMessage("Observer", { "Start observe video call for callee" })
            calleeUseCases.observeVideoCall.invoke(
                sessionId,
                calleeId,
                onReceiveVideoCallRequest = { videoOffer ->
                    onReceiveVideoCallRequest(videoOffer)
                }
            )
            onAcceptCall(sendAcceptCallStatus)
        }
    }

    suspend fun startVideoCall(currentUserId : String?,
                               sessionId : String,
                               remoteVideoOffer : OfferAnswer,
                               onLocalVideoTrackCreated : suspend (localVideoTrack : WebRTCVideoTrack) -> Unit) {
        // Apply the caller's video offer before adding our own local video track.
        // Doing addTrack first on the answerer can create a separate local transceiver
        // that does not bind to the offered video m-line during renegotiation.
        initializeCallUseCase.setRemoteDescription(remoteVideoOffer)
        videoCallUseCase.startVideoCall(
            isVideoInitiator = false,
            onLocalVideoTrackCreated = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
                val callType = getCallTypeFromSdp(remoteVideoOffer.sdp)
                //Create answer
                initializeCallUseCase.createAndSendAnswer(
                    sessionId,
                    callType,
                    currentUserId,
                    object : Utils.Companion.BasicCallBack{
                        override fun onSuccess() {
                            logMessage("createAndSendAnswer", { "success" })
                        }

                        override fun onFailure() {
                            logMessage("createAndSendAnswer", { "failed" })
                        }

                    })
            })
    }
}