package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class CallerUseCases(
    val startCall: StartCallUseCase,
    val sendOffer: SendOfferUseCase,
    val createOffer : CreateOfferUseCase,
    val sendIceCandidate: SendIceCandidateUseCase,
    val observeIceCandidateFromCallee : ObserveIceCandidateUseCase,
    val observeAnswerFromCallee : ObserveAnswer,
    val observeCallStatus : ObserveCallStatus,
    val observeVideoCall : ObserveVideoCall,
    val endCall: EndCallUseCase,
    val sendWhoEndCallUseCase: SendWhoEndCallUseCase
)
class StartCallUseCase(
    private val initializeCallUseCase: InitializeCallUseCase,
    private val signalingUseCase: SendSignalingDataUseCase,
    private val coroutineScope: CoroutineScope
) {
    suspend operator fun invoke(
        session: AudioCallSession,
        onIceCandidateCreated: suspend (IceCandidateData) -> Unit,
        onSendCallSession : (Boolean) -> Unit,
        onError : (ex : Exception) -> Unit
    ) {
        try {
            // 1. Initialize local WebRTC session
            initializeCallUseCase.initializeCall(
                onInitializeFinished = {
                    //Create offer
                    initializeCallUseCase.createOffer(
                        createOfferCallBack = { offer ->
                            session.offer = offer

                            // 2. Send the initial call session to backend
                            coroutineScope.launch {
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
                            }
                        }
                    )
                },
                onIceCandidateCreated = onIceCandidateCreated
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

class CreateOfferUseCase(private val initializeCallUseCase: InitializeCallUseCase) {
    suspend operator fun invoke(onCreateOfferResult : (offer : OfferAnswer) -> Unit){
        initializeCallUseCase.createOffer(
            createOfferCallBack = { offer ->
                onCreateOfferResult(offer)
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
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send ice candidate success
                    logMessage("sendIceCandidateToFireBase", { "send ice candidate success" })
                }

                override fun onFailure() {
                    //Send ice candidate fail
                    logMessage("sendIceCandidateToFireBase", { "send ice candidate fail" })
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
                                onReceiveVideoCallRequest : suspend (videoOffer : OfferAnswer) -> Unit) {
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
    suspend operator fun invoke(sessionId: String) : Boolean{
        return manageCallStateUseCase.endCall(sessionId)
    }
}