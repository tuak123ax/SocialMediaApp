package com.minhtu.firesocialmedia.presentation.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.CallEvent
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.interactor.CallInteractor
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel(
    private val callInteractor: CallInteractor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    var isInCall = MutableStateFlow(false)

    fun updateIsInCall(input: Boolean) {
        isInCall.value = input
    }

    private val _endCallStatus = MutableStateFlow(false)
    val endCallStatus = _endCallStatus.asStateFlow()

    private var _phoneCallRequestStatus = MutableStateFlow<CallingRequestData?>(null)
    val phoneCallRequestStatus = _phoneCallRequestStatus.asStateFlow()

    private var whoStopCall: String = ""
    fun setWhoStopCall(input: String) {
        whoStopCall = input
    }

    fun resetCallEvent() {
        whoStopCall = ""
        CallEventFlow.events.value = null
        updateIsInCall(false)
    }

    fun observePhoneCall(currentUserId: String?) {
        viewModelScope.launch(ioDispatcher) {
            if (currentUserId != null) {
                try {
                    callInteractor.observe(
                        isInCall,
                        currentUserId,
                        onReceivePhoneCallRequest = { callingRequestData ->
                            _phoneCallRequestStatus.value = callingRequestData
                        },
                        whoEndCallCallBack = { whoEndCall ->
                            whoStopCall = whoEndCall
                        },
                        onEndCall = {
                            logMessage("observePhoneCall", { "onEndCall" })
                            _endCallStatus.value = true
                            resetPhoneCallRequestStatus()
                            //Send StopVideoCall first for user who is in video call screen.
                            CallEventFlow.events.value = CallEvent.StopVideoCall
                            //Delay to wait to back to audio call screen.
                            delay(2000)
                            viewModelScope.launch(ioDispatcher) {
                                if (CallEventFlow.events.value != CallEvent.StopCalling &&
                                    CallEventFlow.events.value != CallEvent.CallEnded) {
                                    if (whoStopCall == currentUserId) {
                                        logMessage("observePhoneCall", { "StopCalling whoStopCall from db" })
                                        CallEventFlow.events.value = CallEvent.StopCalling
                                    } else {
                                        if (whoStopCall.isEmpty()) {
                                            logMessage("observePhoneCall", { "whoStopCall is empty" })
                                            if (_phoneCallRequestStatus.value == null) {
                                                logMessage("observePhoneCall", { "StopCalling" })
                                                CallEventFlow.events.value = CallEvent.StopCalling
                                            } else {
                                                logMessage("observePhoneCall", { "CallEnded" })
                                                CallEventFlow.events.value = CallEvent.CallEnded
                                            }
                                        } else {
                                            logMessage("observePhoneCall", { "whoStopCall is not empty" })
                                            logMessage("observePhoneCall", { "StopCalling whoStopCall from db" })
                                            callInteractor.stopCallService()
                                            CallEventFlow.events.value = CallEvent.CallEnded
                                        }
                                    }
                                }
                                isInCall.value = false
                                // Reset all call state after a delay so next call starts clean (if user wasn't on Calling screen to trigger reset there)
                                viewModelScope.launch {
                                    delay(2000L)
                                    CallEventFlow.reset()
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    logMessage("observePhoneCall Exception", { e.message.toString() })
                }
            }
        }
    }

    fun stopObservePhoneCall() {
        callInteractor.stopObservePhoneCall()
    }

    fun resetPhoneCallRequestStatus() {
        _phoneCallRequestStatus.value = null
    }

    fun resetEndCallStatus() {
        _endCallStatus.value = false
    }
}
