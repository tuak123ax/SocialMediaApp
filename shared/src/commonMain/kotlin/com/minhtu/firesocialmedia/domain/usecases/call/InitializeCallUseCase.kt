package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.CallType
import com.minhtu.firesocialmedia.domain.entity.call.IceCandidateData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class InitializeCallUseCase(
    val callRepository: CallRepository,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    suspend fun initializeCall(
        onInitializeFinished : suspend () -> Unit,
        onIceCandidateCreated : suspend (iceCandidateData : IceCandidateData) -> Unit) {
        callRepository.initialize(
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
        callRepository.createVideoOffer(
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
        callRepository.createOffer(
            onOfferCreated = { offer ->
                coroutineScope.launch {
                    //Send offer to DB after created
                    callRepository.sendOfferToFireBase(
                        sessionId,
                        offer,
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
        callRepository.createAnswer(
            callType == CallType.VIDEO,
            onAnswerCreated  = { answer ->
                //Update initiator of answer.
                if(currentUserId != null) {
                    answer.initiator = currentUserId
                }
                coroutineScope.launch {
                    //Send offer to DB after created
                    callRepository.sendAnswerToFirebase(
                        sessionId,
                        answer,
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
        callRepository.setRemoteDescription(offerAnswer)
    }
}