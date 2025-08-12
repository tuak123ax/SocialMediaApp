package com.minhtu.firesocialmedia.presentation.calling.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.CallEventFlow
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallingViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var sessionId = ""
    fun updateSessionId(id : String) {
        sessionId = id
    }
    fun startCall(caller : UserInstance, callee: UserInstance, platform : PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    //Generate session id
                    sessionId = generateSessionId(caller.uid, callee.uid)
                    //Start call service
                    platform.audioCall.startCallService(sessionId, caller, callee)
                } catch (e : Exception) {
                    logMessage("startCall Exception", { e.message.toString() })
                }
            }
        }
    }

    fun stopCall(platform: PlatformContext) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try{
                    if(sessionId.isNotEmpty()) {
                        platform.database.deleteCallSession(
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
                    platform.audioCall.endCallFromApp()
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

    fun requestPermissionAndStartAudioCall(platform: PlatformContext,
                                      onGranted: () -> Unit,
                                      onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) {
                platform.permissionManager.requestAudioPermission()
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
        callee : UserInstance?,
        platform: PlatformContext
    ) {
        viewModelScope.launch(ioDispatcher) {
            platform.audioCall.acceptCallFromApp(sessionId, callee?.uid)
        }
    }

    fun rejectVideoCall(platform: PlatformContext) {
        viewModelScope.launch(ioDispatcher) {
            platform.audioCall.rejectVideoCall()
        }
    }

    fun updateVideoState() {
        CallEventFlow.videoCallState.value = null
    }
}