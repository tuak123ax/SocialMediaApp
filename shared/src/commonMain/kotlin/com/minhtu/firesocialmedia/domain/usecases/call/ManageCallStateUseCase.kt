package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.service.call.AudioCallService
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class ManageCallStateUseCase(
    val audioCallService: AudioCallService,
    val databaseService: DatabaseService
) {
    fun acceptCall(sessionId : String,
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

    suspend fun rejectCall(sessionId: String) {
        if (sessionId.isEmpty()) return

        return suspendCancellableCoroutine { cont ->
            try {
                databaseService.sendCallStatusToFirebase(
                    sessionId,
                    CallStatus.ENDED,
                    Constants.CALL_PATH,
                    object : Utils.Companion.BasicCallBack {
                        override fun onSuccess() {
                            if (cont.isActive) cont.resume(Unit,
                                onCancellation = {
                                    cont.resumeWithException(Exception("Failed to update call status"))
                                }) // Resume when successful
                        }

                        override fun onFailure() {
                            if (cont.isActive) cont.resumeWithException(Exception("Failed to update call status"))
                        }
                    }
                )
            } catch (e: Exception) {
                if (cont.isActive) cont.resumeWithException(e)
            }
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

    suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        audioCallService.acceptCallFromApp(sessionId, calleeId)
    }

    suspend fun endCallFromApp() {
        audioCallService.endCallFromApp()
    }

    suspend fun rejectVideoCall() {
        audioCallService.rejectVideoCall()
    }
}