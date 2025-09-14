package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils

class ManageCallStateUseCase(
    val callRepository: CallRepository
) {
    suspend fun acceptCall(sessionId : String,
                           sendCallStatusCallBack : Utils.Companion.BasicCallBack) {
        //Send accept call status.
        callRepository.sendCallStatusToFirebase(
            sessionId,
            CallStatus.ACCEPTED,
            object : Utils.Companion.BasicCallBack{
                override fun onSuccess() {
                    //Send call status success
                    logMessage("sendCallStatusToFirebase", { "send call status success" })
                    sendCallStatusCallBack.onSuccess()
                }
                override fun onFailure() {
                    //Send call status fail
                    logMessage("sendCallStatusToFirebase", { "send call status fail" })
                    sendCallStatusCallBack.onFailure()
                }
            }
        )
    }

    suspend fun rejectCall(sessionId: String) {
        if (sessionId.isEmpty()) return

        callRepository.sendCallStatusToFirebase(
            sessionId,
            CallStatus.ENDED,
            object : Utils.Companion.BasicCallBack {
                override fun onSuccess() {
                    //Success
                }

                override fun onFailure() {
                    //Fail
                }
            }
        )
    }

    suspend fun endCall(sessionId : String) {
        try{
            //Delete call session on DB when the call ended.
            if(sessionId.isNotEmpty()) {
                callRepository.deleteCallSession(
                    sessionId,
                    object : Utils.Companion.BasicCallBack{
                        override fun onSuccess() {
                            //Delete call session success
                        }

                        override fun onFailure() {
                            //Delete call session fail
                        }
                    }
                )
            }
        } catch (e : Exception) {
            logMessage("Exception when handle end call", { e.message.toString() })
        }
    }

    suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        callRepository.acceptCallFromApp(sessionId, calleeId)
    }

    suspend fun endCallFromApp() {
        callRepository.endCallFromApp()
    }

    suspend fun rejectVideoCall() {
        callRepository.rejectVideoCall()
    }
}