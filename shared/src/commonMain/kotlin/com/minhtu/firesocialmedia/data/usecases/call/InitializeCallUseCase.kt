package com.minhtu.firesocialmedia.data.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.CallEventFlow
import com.minhtu.firesocialmedia.data.model.call.CallType
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.AudioCallService
import com.minhtu.firesocialmedia.domain.DatabaseService
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InitializeCallUseCase(
    val audioCallService: AudioCallService,
    val databaseService: DatabaseService,
    val coroutineScope: CoroutineScope
) {
    fun initializeCall(
        onInitializeFinished : () -> Unit,
        onIceCandidateCreated : (iceCandidateData : IceCandidateData) -> Unit) {
        coroutineScope.launch {
            audioCallService.initialize(
                onInitializeFinished = {
                    onInitializeFinished()
                },
                onIceCandidateCreated = { iceCandidateData ->
                    onIceCandidateCreated(iceCandidateData)
                },
                onRemoteVideoTrackReceived = { remoteVideoTrack ->
                    coroutineScope.launch {
                        //Emit remote video to UI when received
                        CallEventFlow.remoteVideoTrack.emit(remoteVideoTrack)
                    }
                }
            )
        }
    }

    suspend fun createVideoOffer(currentUserId : String?,
                                 videoOfferCreated : (videoOffer : OfferAnswer) -> Unit) {
        audioCallService.createVideoOffer(
            onOfferCreated = { offer ->
                if(currentUserId != null) {
                    //Update initiator
                    offer.initiator = currentUserId
                }
                videoOfferCreated(offer)
            }
        )
    }

    suspend fun createOffer(sessionId : String,
                            sendIceCandidateCallBack: Utils.Companion.BasicCallBack) {
        audioCallService.createOffer(
            onOfferCreated = { offer ->
                coroutineScope.launch {
                    //Send offer to DB after created
                    databaseService.sendOfferToFireBase(
                        sessionId,
                        offer,
                        Constants.CALL_PATH,
                        object : Utils.Companion.BasicCallBack{
                            override fun onSuccess() {
                                //Send offer success
                                sendIceCandidateCallBack.onSuccess()
                            }

                            override fun onFailure() {
                                //Send offer fail
                                sendIceCandidateCallBack.onFailure()
                            }

                        }
                    )
                }
            }
        )
    }

    suspend fun createAnswer(callType : CallType,
                             currentUserId : String?,
                             onAnswerCreated : (answer : OfferAnswer) -> Unit) {
        audioCallService.createAnswer(
            callType == CallType.VIDEO,
            onAnswerCreated  = { answer ->
                //Update initiator of answer.
                if(currentUserId != null) {
                    answer.initiator = currentUserId
                }
                onAnswerCreated(answer)
            }
        )
    }

    suspend fun setRemoteDescription(offerAnswer : OfferAnswer) {
        audioCallService.setRemoteDescription(offerAnswer)
    }
}