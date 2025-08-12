package com.minhtu.firesocialmedia.domain.coordinator

import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
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
        onReceivePhoneCallRequest :  suspend (sessionId : String,
                                     offer : OfferAnswer,
                                     callerId : String,
                                     calleeId : String) -> Unit,
        onEndCall : suspend () -> Unit
    ) {
        logMessage("initialize", { "start initialize" })
        //Callee starts call
        calleeUseCases.listenForIncomingCalls.invoke(
            onInitializeFinished = {
                logMessage("observePhoneCallWithoutCheckingInCall", { "start observe" })
                //Observe phone call request.
                calleeUseCases.observePhoneCall.invoke(
                    calleeId,
                    onReceivePhoneCallRequest = { remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId ->
                        onReceivePhoneCallRequest(remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId)
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
        //Callee set remote description of caller
        calleeUseCases.setRemoteDescription.invoke(offer)
        //Callee send answer
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