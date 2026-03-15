package com.minhtu.firesocialmedia.presentation.calling.audiocall

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.RequestPermissionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallServiceUseCase
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
                    //Generate session id
                    sessionId = generateSessionId(caller.uid, callee.uid)
                    //Start call service
                    startCallServiceUseCase.invoke(
                        sessionId,
                        caller,
                        callee
                    )
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

    /** Single source of truth for mute state (survives recomposition, can be synced later). */
    val isMuted = mutableStateOf(false)

    /** Single source of truth for speaker mode (survives recomposition, can be synced later). */
    val currentSpeakerType = mutableStateOf<SpeakerType>(SpeakerType.Audio)

    /** Resets mute and speaker to defaults (e.g. when call ends). */
    fun resetMuteAndSpeakerState() {
        isMuted.value = false
        currentSpeakerType.value = SpeakerType.Audio
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
        return ssId.ifEmpty { sessionId }
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

    /** Clear video call state after a short delay so navigation and VideoCall screen composition complete first (avoids timing/reset issues). */
    fun clearVideoStateAfterNavigate() {
        viewModelScope.launch {
            delay(200)
            CallEventFlow.videoCallState.value = null
        }
    }

    /** Pending video offer and session for Accept — set when dialog is shown, used when user taps Accept so navigation always has valid data. */
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
    fun stopCallAction(
        currentUser : String,
        isCaller : Boolean
    ) {
        viewModelScope.launch(ioDispatcher) {
            delay(2000L)
            stopCall(isCaller, currentUser)
            resetCounter()
        }
    }

    fun updateMuteStatus(muted: Boolean) {
        isMuted.value = muted
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.updateMuteStatus(muted)
        }
    }

    fun updateSpeakerStatus(speakerType: SpeakerType) {
        currentSpeakerType.value = speakerType
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.updateSpeakerStatus(speakerType)
        }
    }

    /**
     * Pushes current mute and speaker state to the call service.
     * Call when the call becomes active so the device audio matches the UI (fixes no sound until user taps a button).
     */
    fun syncAudioStateToService() {
        viewModelScope.launch(ioDispatcher) {
            manageCallStateUseCase.updateMuteStatus(isMuted.value)
            manageCallStateUseCase.updateSpeakerStatus(currentSpeakerType.value)
        }
    }
}