package com.minhtu.firesocialmedia.core.domain.coordinator.call

import com.minhtu.firesocialmedia.core.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.usecases.call.CallerUseCases
import com.minhtu.firesocialmedia.core.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.VideoCallUseCase

class CallerCoordinator(
    val callerUseCases : CallerUseCases,
    val initializeCallUseCase: InitializeCallUseCase,
    val sendSignalingDataUseCase: SendSignalingDataUseCase,
    val videoCallUseCase: VideoCallUseCase
) {
    suspend fun startCall(
        audioCallSession : AudioCallSession,
        onSendCallSessionResult : (Boolean) -> Unit,
        onRejectVideoCall : suspend () -> Unit,
        onAcceptCall : suspend () -> Unit,
        onReceiveVideoCall : suspend (OfferAnswer) -> Unit,
        onEndCall : suspend () -> Unit
    ) {
        callerUseCases.startCall.invoke(
            audioCallSession,
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
                println("Error happened when start call: ${ex.message}")
            }
        )

        callerUseCases.observeIceCandidateFromCallee.invoke(audioCallSession.sessionId)

        callerUseCases.observeAnswerFromCallee.invoke(
            audioCallSession.sessionId,
            audioCallSession.callerId,
            onRejectVideoCall = {
                onRejectVideoCall()
            }
        )

        callerUseCases.observeCallStatus.invoke(
            audioCallSession.sessionId,
            onAcceptCall = {
                println("Observer: Start observe video call for caller")
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
                val deleteCallSessionResult = callerUseCases.endCall.invoke(audioCallSession.sessionId)
                if(deleteCallSessionResult) {
                    println("DeleteCallSession: DeleteCallSession success by caller")
                    onEndCall()
                }
            }
        )
    }

    suspend fun startVideoCall(
        currentUserId : String?,
        sessionId : String,
        onLocalVideoTrackCreated : suspend (localVideoTrack : Any) -> Unit,
        onRejectVideoCall : suspend () -> Unit = {}
    ) {
        videoCallUseCase.startVideoCall(
            isVideoInitiator = true,
            onLocalVideoTrackCreated = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
                initializeCallUseCase.createVideoOffer(
                    currentUserId,
                    videoOfferCreated = { videoOffer ->
                        sendSignalingDataUseCase.clearAnswerInFirebaseForNewVideoOffer(sessionId)
                        sendSignalingDataUseCase.sendOfferToFireBase(
                            sessionId,
                            videoOffer
                        )
                        sendSignalingDataUseCase.observeAnswerFromCallee(
                            sessionId,
                            currentUserId,
                            expectVideoAnswer = true,
                            onGetAnswerFromCallee = {},
                            onRejectVideoCall = {
                                onRejectVideoCall()
                            }
                        )
                    })
            })
    }
}

