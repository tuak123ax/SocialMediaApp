package com.minhtu.firesocialmedia.domain.usecases.call

import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.repository.CallRepository
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils

class ManageCallStateUseCase(
    val callRepository: CallRepository
) {
    suspend fun acceptCall(sessionId : String) : Boolean{
        //Send accept call status.
        return callRepository.sendCallStatusToFirebase(
            sessionId,
            CallStatus.ACCEPTED
        )
    }

    suspend fun rejectCall(sessionId: String) : Boolean {
        if (sessionId.isEmpty()) return false

        return callRepository.sendCallStatusToFirebase(
            sessionId,
            CallStatus.ENDED
        )
    }

    suspend fun sendWhoEndCall(
        sessionId: String,
        whoEndCall : String) : Boolean {
        if (sessionId.isEmpty()) return false

        return callRepository.sendWhoEndCall(
            sessionId,
            whoEndCall
        )
    }

    suspend fun endCall(sessionId : String) : Boolean{
        return try{
            //Delete call session on DB when the call ended.
            if(sessionId.isNotEmpty()) {
                callRepository.deleteCallSession(
                    sessionId
                )
            } else {
                false
            }
        } catch (e : Exception) {
            logMessage("Exception when handle end call", { e.message.toString() })
            false
        }
    }

    suspend fun acceptCallFromApp(sessionId: String, calleeId: String?) {
        callRepository.acceptCallFromApp(sessionId, calleeId)
    }

    suspend fun callerEndCallFromApp(currentUser : String) {
        callRepository.callerEndCallFromApp(currentUser)
    }

    suspend fun calleeEndCallFromApp(sessionId: String, currentUser : String) {
        callRepository.calleeEndCallFromApp(sessionId, currentUser)
    }

    suspend fun rejectVideoCall() {
        callRepository.rejectVideoCall()
    }
}