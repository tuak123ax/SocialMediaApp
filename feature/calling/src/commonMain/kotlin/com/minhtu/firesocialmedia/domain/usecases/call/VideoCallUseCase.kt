package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VideoCallUseCase(
    val callRepository: CallRepository,
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    suspend fun startVideoCall(
        isVideoInitiator: Boolean,
        onLocalVideoTrackCreated : suspend (localVideoTrack : Any) -> Unit) {
        callRepository.startVideoCall(
            isVideoInitiator = isVideoInitiator,
            onStartVideoCall = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
            }
        )
    }

    suspend fun observeVideoCall(sessionId : String,
                                 callerId : String,
                                 onReceivedVideoCall : suspend (videoOffer : OfferAnswer) -> Unit) {
        callRepository.observeVideoCall(
            sessionId,
            videoCallCallBack = { videoOffer ->
                println("videoCallCallBack: ${videoOffer.initiator}")
                if(callerId != videoOffer.initiator){
                    println("videoCallCallBack: emit offer")
                    coroutineScope.launch { onReceivedVideoCall(videoOffer)}
                }
            }
        )
    }
}
