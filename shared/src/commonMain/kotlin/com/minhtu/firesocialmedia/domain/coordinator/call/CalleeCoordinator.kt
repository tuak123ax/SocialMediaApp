package com.minhtu.firesocialmedia.domain.coordinator.call

import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
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
        onEndCall : suspend () -> Unit
    ) {
        var tempCallingRequestData : CallingRequestData? = null
        //Callee starts call
        calleeUseCases.listenForIncomingCalls.invoke(
            onInitializeFinished = {
                logMessage("observePhoneCallWithoutCheckingInCall", { "start observe" })
                //Observe phone call request.
                calleeUseCases.observePhoneCall.invoke(
                    calleeId,
                    onReceivePhoneCallRequest = { callingRequestData->
                        tempCallingRequestData = callingRequestData
                        onReceivePhoneCallRequest(callingRequestData)
                    },
                    iceCandidateCallBack = { iceCandidates ->
                        //Callee set remote description of caller
                        if(tempCallingRequestData?.offer != null) {
                            logMessage("startCall" , { "setRemoteDescription" })
                            calleeUseCases.setRemoteDescription.invoke(tempCallingRequestData.offer!!)
                        }
                        //Callee set remote ice candidates of caller
                        if(iceCandidates != null) {
                            logMessage("startCall" , { "addIceCandidates" })
                            calleeUseCases.addIceCandidates.invoke(iceCandidates)
                        }
                    },
                    onEndCall = {
                        calleeUseCases.endCallUseCase.invoke(sessionId)
                        onEndCall()
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
        calleeUseCases.acceptCall.invoke(
            sessionId,
            onAcceptCall = { result ->
                if(result) {
                    //Start observe video call for callee
                    logMessage("Observer", { "Start observe video call for callee" })
                    calleeUseCases.observeVideoCall.invoke(
                        sessionId,
                        calleeId,
                        onReceiveVideoCallRequest = { videoOffer ->
                            onReceiveVideoCallRequest(videoOffer)
                        }
                    )
                    onAcceptCall(result)
                }
            }
        )
    }

    suspend fun startVideoCall(currentUserId : String?,
                               sessionId : String,
                               remoteVideoOffer : OfferAnswer,
                               onLocalVideoTrackCreated : suspend (localVideoTrack : WebRTCVideoTrack) -> Unit) {
        videoCallUseCase.startVideoCall(
            onLocalVideoTrackCreated = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
                //This is callee side
                //Set remote description.
                initializeCallUseCase.setRemoteDescription(remoteVideoOffer)
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