package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.call.RequestCameraAndAudioPermissionsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartVideoCallServiceUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoCallViewModel(
    val startVideoCallServiceUseCase: StartVideoCallServiceUseCase,
    val requestCameraAndAudioPermissionsUseCase : RequestCameraAndAudioPermissionsUseCase,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun startVideoCall(
        remoteVideoOffer : OfferAnswerDTO?,
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
}