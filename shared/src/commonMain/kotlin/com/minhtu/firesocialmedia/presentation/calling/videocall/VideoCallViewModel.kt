package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateCameraStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateMicStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.UpdateSpeakerStatusUseCase
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
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun startVideoCall(
        remoteVideoOffer : OfferAnswer?,
        caller : UserInstance,
        callee : UserInstance,
        currentUserId : String?,
        sessionId : String) {
        logMessage("startVideoCall", { sessionId })
        logMessage("startVideoCall", { "currentUserId: $currentUserId" })
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    startVideoCallServiceUseCase.invoke(sessionId, caller, callee, currentUserId, remoteVideoOffer)
                } catch (e : Exception) {
                    logMessage("startVideoCall Exception", { e.message.toString() })
                }
            }
        }
    }

    fun requestPermissionsAndStartVideoCall(
        onGranted: () -> Unit,
        onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) {
                requestCameraAndAudioPermissionsUseCase.invoke()
            }
            if (granted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }

    fun updateMicStatus(micMuted: Boolean) {
        viewModelScope.launch {
            updateMicStatusUseCase.invoke(micMuted)
        }
    }

    fun updateCameraStatus(cameraOff: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            updateCameraStatusUseCase.invoke(cameraOff)
        }
    }

    val currentSpeakerType = mutableStateOf<SpeakerType>(SpeakerType.Audio)
    fun updateSpeakerStatus(speakerType: SpeakerType) {
        currentSpeakerType.value = speakerType
        viewModelScope.launch(ioDispatcher) {
            updateSpeakerStatusUseCase.invoke(speakerType)
        }
    }
}