package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.CallEventFlow
import com.minhtu.firesocialmedia.data.model.call.CallType
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.serviceimpl.call.AudioCallService
import com.minhtu.firesocialmedia.domain.serviceimpl.database.DatabaseService
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InitializeCallUseCase(
    val audioCallService: AudioCallService,
    val databaseService: DatabaseService,
    val coroutineScope: CoroutineScope
) {
    suspend fun initializeCall(
        onInitializeFinished : suspend () -> Unit,
        onIceCandidateCreated : suspend (iceCandidateData : IceCandidateData) -> Unit) {
        audioCallService.initialize(
            onInitializeFinished = {
                coroutineScope.launch {
                    onInitializeFinished()
                }
            },
            onIceCandidateCreated = { iceCandidateData ->
                coroutineScope.launch {
                    onIceCandidateCreated(iceCandidateData)
                }
            },
            onRemoteVideoTrackReceived = { remoteVideoTrack ->
                coroutineScope.launch {
                    CallEventFlow.remoteVideoTrack.emit(remoteVideoTrack)
                }
            }
        )
    }

    suspend fun createVideoOffer(currentUserId : String?,
                                 videoOfferCreated : suspend (videoOffer : OfferAnswer) -> Unit) {
        audioCallService.createVideoOffer(
            onOfferCreated = { offer ->
                coroutineScope.launch {
                    if(currentUserId != null) {
                        //Update initiator
                        offer.initiator = currentUserId
                    }
                    videoOfferCreated(offer)
                }
            }
        )
    }

    suspend fun createAndSendOffer(sessionId : String,
                                   sendOfferCallBack: Utils.Companion.BasicCallBack) {
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
                                sendOfferCallBack.onSuccess()
                            }

                            override fun onFailure() {
                                //Send offer fail
                                sendOfferCallBack.onFailure()
                            }

                        }
                    )
                }
            }
        )
    }

    suspend fun createAndSendAnswer(sessionId : String,
                                    callType : CallType,
                                    currentUserId : String?,
                                    sendAnswerCallBack: Utils.Companion.BasicCallBack) {
        audioCallService.createAnswer(
            callType == CallType.VIDEO,
            onAnswerCreated  = { answer ->
                //Update initiator of answer.
                if(currentUserId != null) {
                    answer.initiator = currentUserId
                }
                coroutineScope.launch {
                    //Send offer to DB after created
                    databaseService.sendAnswerToFirebase(
                        sessionId,
                        answer,
                        Constants.CALL_PATH,
                        object : Utils.Companion.BasicCallBack{
                            override fun onSuccess() {
                                //Send offer success
                                sendAnswerCallBack.onSuccess()
                            }

                            override fun onFailure() {
                                //Send offer fail
                                sendAnswerCallBack.onFailure()
                            }

                        }
                    )
                }
            }
        )
    }

    suspend fun setRemoteDescription(offerAnswer : OfferAnswer) {
        audioCallService.setRemoteDescription(offerAnswer)
    }
}