package com.minhtu.firesocialmedia.presentation.calling.videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoCallViewModel(
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun startVideoCall(
        remoteVideoOffer : OfferAnswer?,
        caller : UserInstance,
        callee : UserInstance,
        currentUserId : String?,
        sessionId : String,
        platform : PlatformContext) {
        logMessage("startVideoCall", { sessionId })
        logMessage("startVideoCall", { "currentUserId: $currentUserId" })
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    platform.audioCall.startVideoCallService(sessionId, caller, callee, currentUserId, remoteVideoOffer)
                } catch (e : Exception) {
                    logMessage("startVideoCall Exception", { e.message.toString() })
                }
            }
        }
    }

    fun requestPermissionsAndStartVideoCall(
        platform: PlatformContext,
        onGranted: () -> Unit,
        onDenied: () -> Unit) {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.IO) {
                platform.permissionManager.requestCameraAndAudioPermissions()
            }
            if (granted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}