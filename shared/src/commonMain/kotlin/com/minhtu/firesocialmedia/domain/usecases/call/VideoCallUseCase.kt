package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class VideoCallUseCase(
    val callRepository: CallRepository,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    suspend fun startVideoCall(
        onLocalVideoTrackCreated : suspend (localVideoTrack : WebRTCVideoTrack) -> Unit) {
        //Start video call
        callRepository.startVideoCall(
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
                logMessage("videoCallCallBack", { videoOffer.initiator })
                if(callerId != videoOffer.initiator){
                    logMessage("videoCallCallBack", { "emit offer" })
                    coroutineScope.launch { onReceivedVideoCall(videoOffer)}
                }
            }
        )
    }
}