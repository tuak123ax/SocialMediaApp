package com.minhtu.firesocialmedia.presentation.calling.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallingViewModel(
    private val startCallServiceUseCase : StartCallServiceUseCase,
    private val manageCallStateUseCase: ManageCallStateUseCase,
    private val requestPermissionUseCase: RequestPermissionUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var sessionId = ""
    fun updateSessionId(id : String) {
        sessionId = id
    }
    fun startCall(caller : UserInstance, callee: UserInstance) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    //Generate session id
                    sessionId = generateSessionId(caller.uid, callee.uid)
                    //Start call service
                    startCallServiceUseCase.invoke(
                        sessionId, caller, callee
                    )
                } catch (e : Exception) {
                    logMessage("startCall Exception", { e.message.toString() })
                }
            }
        }
    }

    fun stopCall() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try{
                    manageCallStateUseCase.endCall(sessionId)
                    manageCallStateUseCase.endCallFromApp()
                } catch(e : Exception) {
                    logMessage("stopCall Exception", { e.message.toString() })
                } finally {
                    sessionId = ""
                }
            }
        }
    }

    fun generateSessionId(callerId: String, calleeId: String): String {
        return listOf(callerId, calleeId).sorted().joinToString("_")
    }

    fun requestPermissionAndStartAudioCall(
        onGranted: () -> Unit,
        onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) {
                requestPermissionUseCase.invoke()
            }
            if (granted) {
                logMessage("requestPermissionAndStartAudioCall", { "granted" })
                onGranted()
            } else {
                logMessage("requestPermissionAndStartAudioCall", { "not granted" })
                onDenied()
            }
        }
    }

    fun getSessionId(ssId : String) : String {
        return if(ssId.isNotEmpty()) {
            ssId
        } else sessionId
    }

    fun acceptCall(
        sessionId : String,
        callee : UserInstance?
    ) {
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.acceptCallFromApp(sessionId, callee?.uid)
        }
    }

    fun rejectVideoCall() {
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.rejectVideoCall()
        }
    }

    fun updateVideoState() {
        CallEventFlow.videoCallState.value = null
    }
}