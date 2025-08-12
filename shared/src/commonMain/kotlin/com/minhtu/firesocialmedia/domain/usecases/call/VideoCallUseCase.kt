package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.DatabaseService
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class VideoCallUseCase(
    val audioCallService: AudioCallService,
    val databaseService: DatabaseService,
    val coroutineScope: CoroutineScope
) {
    suspend fun startVideoCall(
        onLocalVideoTrackCreated : suspend (localVideoTrack : WebRTCVideoTrack) -> Unit) {
        //Start video call
        audioCallService.startVideoCall(
            onStartVideoCall = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
            }
        )
    }

    suspend fun observeVideoCall(sessionId : String,
                                 callerId : String,
                                 onReceivedVideoCall : suspend (videoOffer : OfferAnswer) -> Unit) {
        databaseService.observeVideoCall(
            sessionId,
            Constants.CALL_PATH,
            videoCallCallBack = { videoOffer ->
                logMessage("videoCallCallBack", { videoOffer.initiator })
                if(callerId != videoOffer.initiator){
                    logMessage("videoCallCallBack", { "emit offer" })
                    coroutineScope.launch { onReceivedVideoCall(videoOffer) }
                }
            }
        )
    }
}