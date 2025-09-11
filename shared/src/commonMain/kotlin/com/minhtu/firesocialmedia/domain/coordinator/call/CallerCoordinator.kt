package com.minhtu.firesocialmedia.domain.coordinator.call

import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.usecases.call.CallerUseCases
import com.minhtu.firesocialmedia.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage

class CallerCoordinator(
    val callerUseCases : CallerUseCases,
    val initializeCallUseCase: InitializeCallUseCase,
    val sendSignalingDataUseCase: SendSignalingDataUseCase,
    val videoCallUseCase: VideoCallUseCase
) {
    suspend fun startCall(
        audioCallSession : AudioCallSession,
        onSendOfferResult: (Boolean) -> Unit,
        onSendCallSessionResult : (Boolean) -> Unit,
        onRejectVideoCall : suspend () -> Unit,
        onAcceptCall : suspend () -> Unit,
        onEndCall : suspend () -> Unit,
        onReceiveVideoCall : suspend (OfferAnswerDTO) -> Unit
    ) {
        //Caller starts call
        callerUseCases.startCall.invoke(
            audioCallSession,
            onInitializeFinished = {
                //Create offer
                callerUseCases.sendOffer.invoke(
                    audioCallSession.sessionId,
                    onSendOfferResult = { result ->
                        onSendOfferResult(result)
                    })
            },
            onIceCandidateCreated = { iceCandidateData ->
                callerUseCases.sendIceCandidate.invoke(
                    audioCallSession.sessionId,
                    iceCandidateData,
                    "callerCandidates"
                )
            },
            onSendCallSession = { isSuccess ->
                onSendCallSessionResult(isSuccess)
            },
            onError = { ex ->
                logMessage("Error happened when start call", { ex.message.toString() })
            }
        )

        //Observe ice candidate from callee via DB
        callerUseCases.observeIceCandidateFromCallee.invoke(audioCallSession.sessionId)

        //Observe answer from callee via DB
        callerUseCases.observeAnswerFromCallee.invoke(
            audioCallSession.sessionId,
            audioCallSession.callerId,
            onRejectVideoCall = {
                onRejectVideoCall()
            }
        )

        //Observe call status via DB
        callerUseCases.observeCallStatus.invoke(
            audioCallSession.sessionId,
            onAcceptCall = {
                //Start observe video call for caller
                logMessage("Observer",
                    { "Start observe video call for caller" })

                callerUseCases.observeVideoCall.invoke(
                    audioCallSession.sessionId,
                    audioCallSession.callerId,
                    onReceiveVideoCallRequest = { videoOffer ->
                        onReceiveVideoCall(videoOffer)
                    }
                )
                onAcceptCall()
            },
            onEndCall = {
                callerUseCases.endCall.invoke(audioCallSession.sessionId)
                onEndCall()
            }
        )
    }

    suspend fun startVideoCall(currentUserId : String?,
                               sessionId : String,
                               onLocalVideoTrackCreated : suspend (localVideoTrack : WebRTCVideoTrack) -> Unit) {
        videoCallUseCase.startVideoCall(
            onLocalVideoTrackCreated = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
                //Create video offer
                initializeCallUseCase.createVideoOffer(
                    currentUserId,
                    videoOfferCreated = { videoOffer ->
                        //Send offer to DB after created
                        sendSignalingDataUseCase.sendOfferToFireBase(
                            sessionId,
                            videoOffer
                        )

                        //Observe answer from callee
                        sendSignalingDataUseCase.observeAnswerFromCallee(
                            sessionId,
                            currentUserId,
                            onGetAnswerFromCallee = {
                            },
                            onRejectVideoCall = {
                            }
                        )
                    })
            })
    }
}