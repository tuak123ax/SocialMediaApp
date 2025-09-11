package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils

data class CallerUseCases(
    val startCall: StartCallUseCase,
    val sendOffer: SendOfferUseCase,
    val sendIceCandidate: SendIceCandidateUseCase,
    val observeIceCandidateFromCallee : ObserveIceCandidateUseCase,
    val observeAnswerFromCallee : ObserveAnswer,
    val observeCallStatus : ObserveCallStatus,
    val observeVideoCall : ObserveVideoCall,
    val endCall: EndCallUseCase
)
class StartCallUseCase(
    private val initializeCallUseCase: InitializeCallUseCase,
    private val signalingUseCase: SendSignalingDataUseCase
) {
    suspend operator fun invoke(
        session: AudioCallSession,
        onInitializeFinished: suspend () -> Unit,
        onIceCandidateCreated: suspend (IceCandidateData) -> Unit,
        onSendCallSession : (Boolean) -> Unit,
        onError : (ex : Exception) -> Unit
    ) {
        try {
            // 1. Initialize local WebRTC session
            initializeCallUseCase.initializeCall(
                onInitializeFinished = onInitializeFinished,
                onIceCandidateCreated = onIceCandidateCreated
            )

            // 2. Send the initial call session to backend
            signalingUseCase.sendCallSessionToFirebase(
                session,
                object : Utils.Companion.BasicCallBack {
                    override fun onSuccess() {
                        onSendCallSession(true)
                    }
                    override fun onFailure() {
                        onSendCallSession(false)
                    }
                }
            )
        } catch (e: Exception) {
            onError(e)
        }
    }
}



class SendOfferUseCase(private val initializeCallUseCase: InitializeCallUseCase) {
    suspend operator fun invoke(sessionId : String, onSendOfferResult : (Boolean) -> Unit){
        initializeCallUseCase.createAndSendOffer(
            sessionId,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send offer success
                    onSendOfferResult(true)
                }

                override fun onFailure() {
                    //Send offer fail
                    onSendOfferResult(false)
                }
            }
        )
    }
}

class SendIceCandidateUseCase(private val signalingUseCase: SendSignalingDataUseCase) {
    suspend operator fun invoke(sessionId: String, iceCandidateData: IceCandidateData, whichCandidate : String){
        signalingUseCase.sendIceCandidateToFireBase(
            sessionId,
            iceCandidateData,
            whichCandidate,
            Constants.CALL_PATH,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send ice candidate success
                }

                override fun onFailure() {
                    //Send ice candidate fail
                }

            }
        )
    }
}

class ObserveIceCandidateUseCase(private val signalingUseCase: SendSignalingDataUseCase) {
    suspend operator fun invoke(sessionId: String) {
        signalingUseCase.observeIceCandidateFromCallee(sessionId)
    }
}

class ObserveAnswer(private val signalingUseCase: SendSignalingDataUseCase) {
    suspend operator fun invoke(sessionId: String,
                                callerId : String,
                                onRejectVideoCall : suspend () -> Unit) {
        signalingUseCase.observeAnswerFromCallee(
            sessionId,
            callerId,
            onGetAnswerFromCallee = {
                logMessage("onGetAnswerFromCallee", { "get answer" })
            },
            onRejectVideoCall = {
                onRejectVideoCall()
            }
        )
    }
}

class ObserveCallStatus(private val signalingUseCase: SendSignalingDataUseCase) {
    suspend operator fun invoke(sessionId: String,
                                onAcceptCall : suspend () -> Unit,
                                onEndCall : suspend () -> Unit) {
        signalingUseCase.observeCallStatus(
            sessionId,
            onAcceptCall = {
                onAcceptCall()
            },
            onEndCall = {
                onEndCall()
            }
        )
    }
}

class ObserveVideoCall(private val videoCallUseCase: VideoCallUseCase) {
    suspend operator fun invoke(sessionId: String,
                                callerId : String,
                                onReceiveVideoCallRequest : suspend (videoOffer : OfferAnswerDTO) -> Unit) {
        videoCallUseCase.observeVideoCall(
            sessionId,
            callerId,
            onReceivedVideoCall = { videoOffer ->
                onReceiveVideoCallRequest(videoOffer)
            }
        )
    }
}

class EndCallUseCase(private val manageCallStateUseCase : ManageCallStateUseCase) {
    suspend operator fun invoke(sessionId: String) {
        manageCallStateUseCase.endCall(sessionId)
    }
}