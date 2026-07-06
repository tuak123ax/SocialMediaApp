package com.minhtu.firesocialmedia.presentation.videocall

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.call.UpdateSpeakerStatusUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoCallViewModel(
    val startVideoCallServiceUseCase: StartVideoCallServiceUseCase,
    val requestCameraAndAudioPermissionsUseCase : RequestCameraAndAudioPermissionsUseCase,
    val updateMicStatusUseCase : UpdateMicStatusUseCase,
    val updateCameraStatusUseCase : UpdateCameraStatusUseCase,
    val updateSpeakerStatusUseCase : UpdateSpeakerStatusUseCase,
    val manageCallStateUseCase: ManageCallStateUseCase,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private var lastStartVideoCallSignature: String? = null
    private var isStartingVideoCall: Boolean = false

    fun startVideoCall(
        remoteVideoOffer : OfferAnswer?,
        caller : UserInstance,
        callee : UserInstance,
        currentUserId : String?,
        sessionId : String) {
        val startSignature = "$sessionId|${currentUserId.orEmpty()}|${remoteVideoOffer?.sdp?.hashCode() ?: 0}"
        if (isStartingVideoCall && lastStartVideoCallSignature == startSignature) {
            logMessage("startVideoCall", { "Skip duplicate startVideoCall signature=$startSignature" })
            return
        }
        lastStartVideoCallSignature = startSignature
        isStartingVideoCall = true
        logMessage("startVideoCall", { sessionId })
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    startVideoCallServiceUseCase.invoke(sessionId, caller, callee, currentUserId, remoteVideoOffer)
                } catch (e : Exception) {
                    logMessage("startVideoCall Exception", { e.message.toString() })
                } finally {
                    isStartingVideoCall = false
                }
            }
        }
    }

    fun requestPermissionsAndStartVideoCall(onGranted: () -> Unit, onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) { requestCameraAndAudioPermissionsUseCase.invoke() }
            if (granted) onGranted() else onDenied()
        }
    }

    fun updateMicStatus(micMuted: Boolean) {
        viewModelScope.launch { updateMicStatusUseCase.invoke(micMuted) }
    }

    fun updateCameraStatus(cameraOff: Boolean) {
        viewModelScope.launch(ioDispatcher) { updateCameraStatusUseCase.invoke(cameraOff) }
    }

    val currentSpeakerType = mutableStateOf<SpeakerType>(SpeakerType.Audio)
    fun updateSpeakerStatus(speakerType: SpeakerType) {
        currentSpeakerType.value = speakerType
        viewModelScope.launch(ioDispatcher) { updateSpeakerStatusUseCase.invoke(speakerType) }
    }

    fun stopVideoCallResources() {
        viewModelScope.launch(ioDispatcher) { manageCallStateUseCase.stopVideoCallResources() }
    }

    var pendingVideoCallSessionId = mutableStateOf("")
    var pendingRemoteVideoOffer = mutableStateOf<OfferAnswer?>(null)

    fun setPendingVideoCallParams(sessionId: String, offer: OfferAnswer?) {
        pendingVideoCallSessionId.value = sessionId
        pendingRemoteVideoOffer.value = offer
    }

    fun clearPendingVideoCallParams() {
        pendingVideoCallSessionId.value = ""
        pendingRemoteVideoOffer.value = null
    }
}

