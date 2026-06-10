package com.minhtu.firesocialmedia.feature.calling.presentation.audiocall

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartCallServiceUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
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

    var secondsForCountUpTimer = mutableStateOf(0)
    fun count() {
        secondsForCountUpTimer.value++
    }
    fun resetCounter() {
        secondsForCountUpTimer.value = 0
    }
    fun startCall(caller : UserInstance, callee: UserInstance) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    sessionId = generateSessionId(caller.uid, callee.uid)
                    startCallServiceUseCase.invoke(sessionId, caller, callee)
                } catch (e : Exception) {
                    logMessage("startCall Exception", { e.message.toString() })
                }
            }
        }
    }

    suspend fun stopCall(isCaller : Boolean, currentUser : String) {
        try{
            if(isCaller) {
                logMessage("stopCall", { "stopCall from caller:$currentUser" })
                manageCallStateUseCase.callerEndCallFromApp(currentUser)
            } else {
                logMessage("stopCall", { "stopCall from callee:$currentUser" })
                manageCallStateUseCase.calleeEndCallFromApp(sessionId, currentUser)
            }
        } catch(e : Exception) {
            logMessage("stopCall Exception", { e.message.toString() })
        } finally {
            sessionId = ""
            resetMuteAndSpeakerState()
        }
    }

    val isMuted = mutableStateOf(false)
    val currentSpeakerType = mutableStateOf<SpeakerType>(SpeakerType.Audio)

    fun resetMuteAndSpeakerState() {
        isMuted.value = false
        currentSpeakerType.value = SpeakerType.Audio
    }

    fun generateSessionId(callerId: String, calleeId: String): String {
        return listOf(callerId, calleeId).sorted().joinToString("_")
    }

    fun requestPermissionAndStartAudioCall(onGranted: () -> Unit, onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) { requestPermissionUseCase.invoke() }
            if (granted) {
                logMessage("requestPermissionAndStartAudioCall", { "granted" })
                onGranted()
            } else {
                logMessage("requestPermissionAndStartAudioCall", { "not granted" })
                onDenied()
            }
        }
    }

    fun getSessionId(ssId : String) : String = ssId.ifEmpty { sessionId }

    fun acceptCall(sessionId : String, callee : UserInstance?) {
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.acceptCallFromApp(sessionId, callee?.uid)
        }
    }

    fun rejectVideoCall() {
        viewModelScope.launch(ioDispatcher) { manageCallStateUseCase.rejectVideoCall() }
    }

    fun updateVideoState() { CallEventFlow.videoCallState.value = null }

    fun clearVideoStateAfterNavigate() {
        viewModelScope.launch {
            delay(200)
            CallEventFlow.videoCallState.value = null
        }
    }

    var pendingVideoOfferForAccept = mutableStateOf<OfferAnswer?>(null)
    var pendingSessionIdForVideoCall = mutableStateOf("")

    fun setPendingVideoOfferForAccept(offer: OfferAnswer?, sessionIdForCall: String) {
        pendingVideoOfferForAccept.value = offer
        pendingSessionIdForVideoCall.value = sessionIdForCall
    }

    fun clearPendingVideoOfferForAccept() {
        pendingVideoOfferForAccept.value = null
        pendingSessionIdForVideoCall.value = ""
    }

    fun stopCallAction(currentUser : String, isCaller : Boolean) {
        viewModelScope.launch(ioDispatcher) {
            delay(2000L)
            stopCall(isCaller, currentUser)
            resetCounter()
        }
    }

    fun updateMuteStatus(muted: Boolean) {
        isMuted.value = muted
        viewModelScope.launch(ioDispatcher) { manageCallStateUseCase.updateMuteStatus(muted) }
    }

    fun updateSpeakerStatus(speakerType: SpeakerType) {
        currentSpeakerType.value = speakerType
        viewModelScope.launch(ioDispatcher) { manageCallStateUseCase.updateSpeakerStatus(speakerType) }
    }

    fun syncAudioStateToService() {
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.updateMuteStatus(isMuted.value)
            manageCallStateUseCase.updateSpeakerStatus(currentSpeakerType.value)
        }
    }
}

