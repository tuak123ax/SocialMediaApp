package com.minhtu.firesocialmedia.presentation.audiocall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.call.CallEvent
import com.minhtu.firesocialmedia.core.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.call.SpeakerType
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toHex
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.core.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.Utils.Companion.sendNotification
import com.minhtu.sharedmodule.ui.theme.callAcceptColor
import com.minhtu.sharedmodule.ui.theme.callAcceptContainerColor
import com.minhtu.sharedmodule.ui.theme.callAcceptOnColor
import com.minhtu.sharedmodule.ui.theme.callStopPendingColor
import com.minhtu.sharedmodule.ui.theme.iconButtonBackgroundColor
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Calling {
    companion object{
        @Composable
        fun CallingScreen(
            localImageLoaderValue : ProvidedValue<*>,
            sessionId : String,
            callee : UserInstance,
            caller : UserInstance,
            currentUser : UserInstance?,
            remoteOffer : OfferAnswer?,
            navigateToCallingScreenFromNotification : Boolean,
            callingViewModel: CallingViewModel,
            homeViewModel: HomeViewModelContract,
            navHandler : NavigationHandler,
            onStopCallAndNavigateBack : () -> Unit,
            onNavigateToVideoCall : (sessionId : String, videoOffer : OfferAnswer?) -> Unit,
            modifier: Modifier){
            val isCalling = (currentUser == caller)
            var startCount by rememberSaveable { mutableStateOf(false) }
            var isRunning by rememberSaveable { mutableStateOf(false) }
            var acceptCall by rememberSaveable { mutableStateOf(false) }
            val showDialog = remember { mutableStateOf(false) }
            var backgroundButton by remember { mutableStateOf<Color?>(null) }

            val isMuted by callingViewModel.isMuted
            val currentSpeakerType by callingViewModel.currentSpeakerType
            var isStopCallPending by remember { mutableStateOf(false) }

            LaunchedEffect(acceptCall, isCalling) {
                if (acceptCall || isCalling) {
                    callingViewModel.syncAudioStateToService()
                }
            }

            LaunchedEffect(Unit) {
                countDownTimer(
                    onTimeOver = {
                        backgroundButton = null
                        isRunning = false
                        callingViewModel.stopCallAction(currentUser!!.uid, isCalling)
                        sendNotification("", sessionId, caller, callee, "STOP_CALL")
                    },
                    isCallAccepted = { acceptCall }
                )
                callingViewModel.requestPermissionAndStartAudioCall(
                    onGranted = {
                        if(!startCount) {
                            logMessage("grantPermission", { "Granted" })
                            callingViewModel.updateSessionId(sessionId)
                            logMessage("grantPermission", { "caller and callee not null" })
                            if(currentUser == caller) {
                                if(!navigateToCallingScreenFromNotification){
                                    callingViewModel.startCall(caller, callee)
                                } else {
                                    logMessage("navigateToCallingScreenFromNotification", { "caller start timer" })
                                    startCount = true
                                    isRunning = true
                                    acceptCall = true
                                }
                            } else if(currentUser == callee && navigateToCallingScreenFromNotification) {
                                startCount = true
                                isRunning = true
                                acceptCall = true
                            }
                        }
                    },
                    onDenied = {
                        logMessage("grantPermission", { "not Granted" })
                        showToast("Permission is denied! Return to previous screen.")
                        navHandler.navigateBack()
                    }
                )
            }

            val callEventState by CallEventFlow.events.collectAsState()
            LaunchedEffect(callEventState) {
                if(callEventState != null) {
                    when(callEventState) {
                        CallEvent.AnswerReceived -> {
                            if(currentUser?.uid != callee.uid) {
                                showToast(callee.name + " accepted your call!")
                            }
                            startCount = true
                            isRunning = true
                            acceptCall = true
                        }
                        CallEvent.CallEnded -> {
                            if(callee.name == currentUser?.name) {
                                showToast(caller.name + " stopped the call!")
                            } else {
                                showToast(callee.name + " stopped the call!")
                            }
                            startCount = false
                            isRunning = false
                            acceptCall = false
                            callingViewModel.resetMuteAndSpeakerState()
                            CallEventFlow.reset()
                            onStopCallAndNavigateBack()
                        }
                        CallEvent.StopCalling -> {
                            logMessage("CallEvent", { "StopCalling" })
                            showToast("You stopped the call!")
                            startCount = false
                            isRunning = false
                            acceptCall = false
                            callingViewModel.resetMuteAndSpeakerState()
                            CallEventFlow.reset()
                            onStopCallAndNavigateBack()
                        }
                        else -> {}
                    }
                    CallEventFlow.events.value = null
                }
            }

            val videoCallState by CallEventFlow.videoCallState.collectAsState()
            val hasAcceptedVideoInCurrentCall by CallEventFlow.hasAcceptedVideoInCurrentCall.collectAsState()
            var pendingVideoOffer by remember { mutableStateOf<OfferAnswer?>(null) }

            LaunchedEffect(videoCallState) {
                if (videoCallState != null) {
                    logMessage("videoCallState", { "videoOffer not null" })
                    logMessage("videoCallState", { videoCallState!!.initiator })
                    pendingVideoOffer = videoCallState
                    callingViewModel.setPendingVideoOfferForAccept(videoCallState, callingViewModel.getSessionId(sessionId))
                    if (hasAcceptedVideoInCurrentCall) {
                        val sid = callingViewModel.pendingSessionIdForVideoCall.value.ifEmpty { callingViewModel.getSessionId(sessionId) }
                        if (sid.isNotEmpty()) {
                            onNavigateToVideoCall(sid, videoCallState)
                            callingViewModel.clearVideoStateAfterNavigate()
                            callingViewModel.clearPendingVideoOfferForAccept()
                            pendingVideoOffer = null
                        } else {
                            showDialog.value = true
                        }
                    } else {
                        showDialog.value = true
                    }
                } else {
                    logMessage("videoCallState", { "videoOffer null" })
                    showDialog.value = false
                }
            }

            IncomingCallBottomSheet(
                callerName = if(isCalling) callee.name else caller.name,
                callerImage = if(isCalling) callee.image.toStorageUrl() else caller.image.toStorageUrl(),
                localImageLoaderValue,
                onAccept = {
                    val offer = callingViewModel.pendingVideoOfferForAccept.value
                        ?: pendingVideoOffer
                        ?: videoCallState
                    val sid = callingViewModel.pendingSessionIdForVideoCall.value.ifEmpty { callingViewModel.getSessionId(sessionId) }
                    if (offer != null && sid.isNotEmpty()) {
                        CallEventFlow.hasAcceptedVideoInCurrentCall.value = true
                        onNavigateToVideoCall(sid, offer)
                        callingViewModel.clearVideoStateAfterNavigate()
                    } else {
                        callingViewModel.updateVideoState()
                    }
                    callingViewModel.clearPendingVideoOfferForAccept()
                    pendingVideoOffer = null
                },
                onDecline = {
                    callingViewModel.clearPendingVideoOfferForAccept()
                    pendingVideoOffer = null
                    callingViewModel.rejectVideoCall()
                    callingViewModel.updateVideoState()
                },
                onDismiss = {
                    callingViewModel.clearPendingVideoOfferForAccept()
                    pendingVideoOffer = null
                    callingViewModel.rejectVideoCall()
                    callingViewModel.updateVideoState()
                },
                showDialog
            )
            Column(modifier = modifier.padding(vertical = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                logMessage("CallingScreen", { "Calling Screen is shown" })
                CompositionLocalProvider(localImageLoaderValue) {
                    Surface(
                        shape = CircleShape,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.size(150.dp).padding(20.dp)
                    ) {
                        AutoSizeImage(
                            if (isCalling) callee.image else caller.image,
                            contentDescription = "image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                .testTag(TestTag.TAG_USER_AVATAR)
                                .semantics { contentDescription = TestTag.TAG_USER_AVATAR }
                        )
                    }
                }
                Text(
                    text = if(isCalling) callee.name else caller.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = if(isCalling) "You are calling..." else "is calling you",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                )
                if(startCount) {
                    Spacer(modifier = Modifier.height(20.dp))
                    val callDurationSeconds by CallEventFlow.callDurationSeconds.collectAsState()
                    CountUpTimer(seconds = callDurationSeconds, isRunning = isRunning)
                    Spacer(modifier = Modifier.height(20.dp))
                    CallActionButton(
                        "video_call", "VIDEO CALL", "Video Call Button",
                        modifier = Modifier.testTag(TestTag.TAG_VIDEO_CALL_BUTTON)
                            .semantics { contentDescription = TestTag.TAG_VIDEO_CALL_BUTTON },
                        onClickButton = {
                            if (!isStopCallPending) onNavigateToVideoCall(callingViewModel.sessionId, null)
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if(isCalling || acceptCall) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        CallActionButton(
                            if(!isMuted) "unmute" else "mute",
                            if(!isMuted) "MUTE" else "UNMUTE",
                            "Mute button",
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_MUTE)
                                .semantics { contentDescription = TestTag.TAG_BUTTON_MUTE },
                            onClickButton = {
                                if (!isStopCallPending) callingViewModel.updateMuteStatus(!isMuted)
                            }
                        )
                        CallActionButton(
                            when(currentSpeakerType) { SpeakerType.Audio -> "audio"; SpeakerType.Speaker -> "speaker" },
                            when(currentSpeakerType) { SpeakerType.Audio -> "AUDIO"; SpeakerType.Speaker -> "SPEAKER" },
                            "Speaker button",
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_SPEAKER)
                                .semantics { contentDescription = TestTag.TAG_BUTTON_SPEAKER },
                            onClickButton = {
                                if (!isStopCallPending) {
                                    val nextSpeakerType = when(currentSpeakerType) {
                                        SpeakerType.Audio -> SpeakerType.Speaker
                                        SpeakerType.Speaker -> SpeakerType.Audio
                                    }
                                    callingViewModel.updateSpeakerStatus(nextSpeakerType)
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp)) {
                    if(!isCalling) {
                        if(!acceptCall) {
                            if(!navigateToCallingScreenFromNotification) {
                                FloatingActionButton(
                                    onClick = {
                                        if (!isStopCallPending) {
                                            startCount = true; isRunning = true; acceptCall = true
                                            if(currentUser == callee && remoteOffer != null) {
                                                callingViewModel.acceptCall(sessionId, callee)
                                            }
                                            callingViewModel.resetCounter()
                                        }
                                    },
                                    modifier = Modifier.size(56.dp).testTag(TestTag.TAG_ACCEPT_CALL_BUTTON)
                                        .semantics { contentDescription = TestTag.TAG_ACCEPT_CALL_BUTTON },
                                    shape = CircleShape,
                                    containerColor = callAcceptColor,
                                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Accept Call", tint = callAcceptOnColor)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    LaunchedEffect(isStopCallPending) {
                        if (isStopCallPending) {
                            isRunning = false
                            callingViewModel.stopCallAction(currentUser!!.uid, isCalling)
                            if (isCalling) {
                                sendNotification("", sessionId, caller, callee, "STOP_CALL")
                            } else {
                                sendNotification("", sessionId, callee, caller, "STOP_CALL")
                            }
                            homeViewModel.setWhoStopCall(currentUser.uid)
                            isStopCallPending = false
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            if (!isStopCallPending) { backgroundButton = callStopPendingColor; isStopCallPending = true }
                        },
                        modifier = Modifier.size(56.dp).testTag(TestTag.TAG_REJECT_CALL_BUTTON)
                            .semantics { contentDescription = TestTag.TAG_REJECT_CALL_BUTTON },
                        shape = CircleShape,
                        containerColor = backgroundButton ?: MaterialTheme.colorScheme.error,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Reject Call", tint = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }

        fun getScreenName() : String = "CallingScreen"

        @Composable
        fun CountUpTimer(seconds : Int, isRunning: Boolean = true) {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            val formattedTime = buildString {
                append(if (hours < 10) "0$hours" else "$hours")
                append(":")
                append(if (minutes < 10) "0$minutes" else "$minutes")
                append(":")
                append(if (secs < 10) "0$secs" else "$secs")
            }
            Text(text = formattedTime, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        }

        fun countDownTimer(onTimeOver: () -> Unit, isCallAccepted: () -> Boolean) {
            val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            backgroundScope.launch {
                var seconds = 60
                while (seconds != 0) { delay(1000L); seconds-- }
                if (!isCallAccepted()) { onTimeOver() }
            }
        }

        @Composable
        fun CallActionButton(icon : String, text : String, description : String, modifier: Modifier = Modifier, onClickButton : () -> Unit) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier.size(56.dp).clip(CircleShape).background(iconButtonBackgroundColor).clickable { onClickButton() }
                ) {
                    CrossPlatformIcon(
                        icon = icon,
                        backgroundColor = iconButtonBackgroundColor.toHex(),
                        contentDescription = description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(30.dp).padding(4.dp)
                    )
                }
                Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun IncomingCallBottomSheet(
            callerName: String, callerImage: String, localImageLoaderValue : ProvidedValue<*>,
            onAccept: () -> Unit, onDecline: () -> Unit, onDismiss: () -> Unit, showDialog: MutableState<Boolean>
        ) {
            if (showDialog.value) {
                ModalBottomSheet(
                    onDismissRequest = { showDialog.value = false; onDismiss() },
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Box(modifier = Modifier.padding(top = 8.dp).width(40.dp).height(4.dp)
                            .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)))
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(150.dp).clip(CircleShape).background(callAcceptContainerColor))
                            CompositionLocalProvider(localImageLoaderValue) {
                                Surface(shape = CircleShape, tonalElevation = 6.dp, shadowElevation = 6.dp,
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.size(150.dp).padding(20.dp)) {
                                    AutoSizeImage(callerImage, contentDescription = "image", contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            .testTag(TestTag.TAG_USER_AVATAR)
                                            .semantics { contentDescription = TestTag.TAG_USER_AVATAR })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = callerName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(callAcceptColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Incoming Video Call", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(36.dp))
                        Button(onClick = { showDialog.value = false; onAccept() },
                            modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = callAcceptColor)) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = callAcceptOnColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accept Call", fontWeight = FontWeight.Bold, color = callAcceptOnColor)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { showDialog.value = false; onDecline() },
                            modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))) {
                            Icon(Icons.Default.CallEnd, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Decline", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

