package com.minhtu.firesocialmedia.domain.coordinator.call

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.usecases.call.CalleeUseCases
import com.minhtu.firesocialmedia.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.domain.entity.call.getCallTypeFromSdp

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
        calleeUseCases.listenForIncomingCalls.invoke(
            onInitializeFinished = {
                println("observePhoneCallWithoutCheckingInCall: start observe")
                calleeUseCases.observePhoneCall.invoke(
                    calleeId,
                    onReceivePhoneCallRequest = { callingRequestData->
                        onReceivePhoneCallRequest(callingRequestData)
                    },
                    iceCandidateCallBack = { iceCandidates ->
                        if(iceCandidates != null) {
                            println("startCall: addIceCandidates")
                            calleeUseCases.addIceCandidates.invoke(iceCandidates)
                        }
                    },
                    onEndCall = {
                        onEndCall()
                    },
                    whoEndCallCallBack = { whoEndCall ->
                        whoEndCallCallBack(whoEndCall)
                    }
                )
            },
            onIceCandidateCreated = { iceCandidateData ->
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
        onReceiveVideoCallRequest : suspend (OfferAnswer) -> Unit
    ) {
        initializeCallUseCase.setRemoteDescription(offer)

        calleeUseCases.sendAnswer.invoke(
            sessionId,
            offer,
            onSendAnswerResult = { result ->
                if(result) {
                    println("onSendAnswerResult: success")
                } else {
                    println("onSendAnswerResult: fail")
                }
            }
        )
        val sendAcceptCallStatus = calleeUseCases.acceptCall.invoke(sessionId)
        if(sendAcceptCallStatus) {
            println("Observer: Start observe video call for callee")
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

    suspend fun startVideoCall(
        currentUserId : String?,
        sessionId : String,
        remoteVideoOffer : OfferAnswer,
        onLocalVideoTrackCreated : suspend (localVideoTrack : Any) -> Unit
    ) {
        initializeCallUseCase.setRemoteDescription(remoteVideoOffer)
        videoCallUseCase.startVideoCall(
            isVideoInitiator = false,
            onLocalVideoTrackCreated = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
                val callType = getCallTypeFromSdp(remoteVideoOffer.sdp)
                initializeCallUseCase.createAndSendAnswer(
                    sessionId,
                    callType,
                    currentUserId,
                    object : Utils.Companion.BasicCallBack {
                        override fun onSuccess() {
                            println("createAndSendAnswer: success")
                        }
                        override fun onFailure() {
                            println("createAndSendAnswer: failed")
                        }
                    })
            })
    }
}

