package com.minhtu.firesocialmedia.data.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.domain.AudioCallService
import com.minhtu.firesocialmedia.domain.DatabaseService
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope

class ManageCallStateUseCase(
    val audioCallService: AudioCallService,
    val databaseService: DatabaseService,
    val coroutineScope: CoroutineScope
) {
    suspend fun acceptCall(sessionId : String,
                           sendCallStatusCallBack : Utils.Companion.BasicCallBack) {
        //Send accept call status.
        databaseService.sendCallStatusToFirebase(
            sessionId,
            CallStatus.ACCEPTED,
            Constants.CALL_PATH,
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

            })
    }

    suspend fun rejectCall(sessionId : String) {
        try{
            //Update call status in DB when reject call.
            if(sessionId.isNotEmpty()) {
                databaseService.sendCallStatusToFirebase(
                    sessionId,
                    CallStatus.ENDED,
                    Constants.CALL_PATH,
                    object : Utils.Companion.BasicCallBack{
                        override fun onSuccess() {
                            //Send call status success
                        }

                        override fun onFailure() {
                            //Send call status fail
                        }

                    })
            }
        } catch (e : Exception) {
            logMessage("Exception when handle reject call", { e.message.toString() })
        }
    }

    suspend fun endCall(sessionId : String) {
        try{
            //Delete call session on DB when the call ended.
            if(sessionId.isNotEmpty()) {
                databaseService.deleteCallSession(
                    sessionId,
                    Constants.CALL_PATH,
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
}