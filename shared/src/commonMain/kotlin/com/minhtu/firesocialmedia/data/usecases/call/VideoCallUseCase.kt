package com.minhtu.firesocialmedia.data.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.CallEventFlow
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.AudioCallService
import com.minhtu.firesocialmedia.domain.DatabaseService
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
        onLocalVideoTrackCreated : (localVideoTrack : WebRTCVideoTrack) -> Unit) {
        //Start video call
        audioCallService.startVideoCall(
            onStartVideoCall = { localVideoTrack ->
                onLocalVideoTrackCreated(localVideoTrack)
            }
        )
    }

    suspend fun observeVideoCall(sessionId : String,
                                 calleeId : String,
                                 onReceivedVideoCall : (videoOffer : OfferAnswer) -> Unit) {
        databaseService.observeVideoCall(
            sessionId,
            Constants.CALL_PATH,
            videoCallCallBack = { videoOffer ->
                logMessage("videoCallCallBack", { videoOffer.initiator })
                if(calleeId != videoOffer.initiator){
                    logMessage("videoCallCallBack", { "emit offer" })
                    onReceivedVideoCall(videoOffer)
                }
            }
        )
    }
}