package com.minhtu.firesocialmedia.presentation.calling.audiocall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.call.CallEvent
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils.Companion.sendNotification
import com.minhtu.firesocialmedia.utils.Utils.Companion.stopCallAction
import com.seiko.imageloader.LocalImageLoader
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
            platform : PlatformContext,
            sessionId : String,
            callee : UserInstance?,
            caller : UserInstance?,
            currentUser : UserInstance?,
            remoteOffer : OfferAnswerDTO?,
            navigateToCallingScreenFromNotification : Boolean,
            callingViewModel: CallingViewModel,
            navHandler : NavigationHandler,
            onStopCall : () -> Unit,
            onNavigateToVideoCall : (sessionId : String, videoOffer : OfferAnswerDTO?) -> Unit,
            modifier: Modifier){
            val coroutineScope = rememberCoroutineScope()
            val isCalling = (currentUser == caller)
            //Start count up timer and show video call button
            var startCount by rememberSaveable { mutableStateOf(false) }
            //Start/stop count up timer
            var isRunning by rememberSaveable { mutableStateOf(false) }
            //Check accept call to update UI
            var acceptCall by rememberSaveable { mutableStateOf(false) }
            //Show dialog to accept video call
            val showDialog = remember { mutableStateOf(false) }
            var videoOffer : OfferAnswerDTO? = null

            LaunchedEffect(Unit) {
//                countDownTimer(onTimeOver = {
//                    stopCallAction(
//                        callingViewModel,
//                        coroutineScope,
//                        navHandler,
//                        onStopCall,
//                        platform
//                    )
//                })
                callingViewModel.requestPermissionAndStartAudioCall(
                    onGranted = {
                        if(!startCount) {
                            logMessage("grantPermission", { "Granted" })
                            callingViewModel.updateSessionId(sessionId)
                            if(caller != null && callee != null) {
                                logMessage("grantPermission", { "caller and callee not null" })
                                if(currentUser == caller) {
                                    if(!navigateToCallingScreenFromNotification){
                                        callingViewModel.startCall(caller, callee)
                                    } else {
                                        logMessage("navigateToCallingScreenFromNotification",
                                            { "caller start timer" })
                                        startCount = true
                                        isRunning = true
                                        acceptCall = true
                                    }
                                } else {
                                    if(currentUser == callee && navigateToCallingScreenFromNotification) {
                                        logMessage("navigateToCallingScreenFromNotification",
                                            { "callee start timer" })
                                        startCount = true
                                        isRunning = true
                                        acceptCall = true
                                    }
                                }
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

            LaunchedEffect(Unit) {
                //Observe answer
                CallEventFlow.events.collect { event ->
                    when(event) {
                        CallEvent.AnswerReceived -> {
                            if(currentUser?.uid != callee?.uid) {
                                showToast(callee?.name + " accepted your call!")
                            }
                            startCount = true
                            isRunning = true
                            acceptCall = true
                        }

                         CallEvent.CallEnded -> {
                             if(currentUser?.uid != callee?.uid) {
                                 showToast(callee?.name + " stopped your call!")
                             }
                             isRunning = false
                             stopCallAction(
                                 callingViewModel,
                                 coroutineScope,
                                 onStopCall,
                                 platform,
                                 navHandler
                             )
                         }

                        CallEvent.StopCalling -> {
                            logMessage("CallEvent", { "StopCalling" })
                            showToast("You stopped the call!")
//                            isRunning = false
//                            stopCallAction(
//                                callingViewModel,
//                                coroutineScope,
//                                onStopCall,
//                                platform,
//                                navHandler
//                            )
                        }
                    }
                }
            }

            //Observe video call request
            val videoCallState by CallEventFlow.videoCallState.collectAsState()

            LaunchedEffect(videoCallState) {
                if (videoCallState != null) {
                    //Received video call request, show dialog.
                    logMessage("videoCallState", { "videoOffer not null" })
                    logMessage("videoCallState", { videoCallState!!.initiator })
                    showDialog.value = true
                    videoOffer = videoCallState
                } else {
                    //No video call request
                    logMessage("videoCallState", { "videoOffer null" })
                    showDialog.value = false
                }
            }

            //Dialog to accept/reject video call
            UiUtils.ShowBasicAlertDialog(
                "Video Call",
                "Other person want to make a video call. Do you want to join?",
                onClickConfirm = {
                    logMessage("onClickConfirm", { "Confirm" })
                    if(videoOffer != null) {
                        onNavigateToVideoCall(callingViewModel.getSessionId(sessionId), videoOffer)
                    }
                    callingViewModel.updateVideoState()
                },
                onClickReject = {
                    callingViewModel.rejectVideoCall()
                    callingViewModel.updateVideoState()
                },
                showDialog
            )
            Column(modifier = modifier.padding(vertical = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                //Avatar
                CompositionLocalProvider(
                    LocalImageLoader provides remember { generateImageLoader() },
                ) {
                    AutoSizeImage(
                        if(isCalling) callee!!.image else caller!!.image,
                        contentDescription = "image",
                        contentScale = ContentScale.Companion.Crop,
                        modifier = Modifier.Companion
                            .size(120.dp)
                            .clip(CircleShape) // Ensures circular shape
                            .border(
                                2.dp,
                                Color.Companion.White,
                                CircleShape
                            ) // Optional border for better appearance
                            .testTag(TestTag.Companion.TAG_USER_AVATAR)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_USER_AVATAR
                            }
                    )
                }
                Spacer(modifier = Modifier.Companion.height(10.dp)) // Space between avatar and name
                // User name with max width & ellipsis
                Text(
                    text = if(isCalling) callee!!.name else caller!!.name,
                    color = Color.Companion.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth(), // Restrict width to avoid touching buttons
                    overflow = TextOverflow.Companion.Ellipsis, // Add "..." if too long
                    maxLines = 1
                )

                Spacer(modifier = Modifier.Companion.height(20.dp))
                Text(
                    text = if(isCalling) "You are calling..." else "is calling you",
                    color = Color.Companion.Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth() // Restrict width to avoid touching buttons
                )
                //Audio call is happening, start count-up timer
                if(startCount) {
                    Spacer(modifier = Modifier.Companion.height(20.dp))
                    //Count-up timer
                    CountUpTimer(onTick = {},
                        isRunning)
                    Spacer(modifier = Modifier.height(20.dp))
                    //Video call button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape) // Shadow before clipping
                            .clip(CircleShape)
                            .background(Color.White)
                            .testTag(TestTag.TAG_VIDEO_CALL_BUTTON)
                            .semantics {
                                contentDescription = TestTag.TAG_VIDEO_CALL_BUTTON
                            }
                    ) {
                        FloatingActionButton(
                            onClick = {
                                onNavigateToVideoCall(callingViewModel.sessionId, null)
//                                callingViewModel.observeAnswerFromCallee(
//                                    platform,
//                                    onGetAnswerFromCallee = {
//                                    })
                            },
                            shape = CircleShape,
                            containerColor = Color.Transparent, // Transparent to let gradient show
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = "VideoCall", tint = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                //Accept and Reject button
                Row(horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp)) {
                    if(!isCalling) {
                        if(!acceptCall) {
                            if(!navigateToCallingScreenFromNotification) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .shadow(8.dp, CircleShape) // Shadow before clipping
                                        .clip(CircleShape)
                                        .background(Color.Green)
                                        .testTag(TestTag.TAG_ACCEPT_CALL_BUTTON)
                                        .semantics {
                                            contentDescription = TestTag.TAG_ACCEPT_CALL_BUTTON
                                        }
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            startCount = true
                                            isRunning = true
                                            acceptCall = true
                                            if(currentUser == callee) {
                                                if(remoteOffer != null) {
                                                    callingViewModel.acceptCall(
                                                        sessionId,
                                                        callee
                                                    )
                                                }
                                            }
                                        },
                                        shape = CircleShape,
                                        containerColor = Color.Transparent, // Transparent to let gradient show
                                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Accept Call", tint = Color.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    var backgroundButton by remember {mutableStateOf(Color.Red)}
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape) // Shadow before clipping
                            .clip(CircleShape)
                            .background(backgroundButton)
                            .testTag(TestTag.TAG_REJECT_CALL_BUTTON)
                            .semantics {
                                contentDescription = TestTag.TAG_REJECT_CALL_BUTTON
                            }
                    ) {
                        FloatingActionButton(
                            onClick = {
                                backgroundButton = Color.Gray
                                isRunning = false
                                stopCallAction(
                                    callingViewModel,
                                    coroutineScope,
                                    onStopCall,
                                    platform,
                                    navHandler
                                )
                                if(caller != null && callee != null) {
                                    sendNotification("", sessionId, caller, callee, "STOP_CALL")
                                }
                            },
                            shape = CircleShape,
                            containerColor = Color.Transparent, // Transparent to let gradient show
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Reject Call", tint = Color.Black)
                        }
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "CallingScreen"
        }

        @Composable
        fun CountUpTimer(
            onTick: ((Int) -> Unit)? = null,
            isRunning: Boolean = true
        ) {
            var seconds by remember { mutableStateOf(0) }

            LaunchedEffect(isRunning) {
                while (isRunning) {
                    delay(1000L)
                    seconds++
                    onTick?.invoke(seconds)
                }
            }

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
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        fun countDownTimer(
            onTimeOver: () -> Unit,
            isRunning: Boolean = true
        ) {
            val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            backgroundScope.launch {
                var seconds = 30
                while (seconds != 0) {
                    delay(1000L)
                    seconds--
                }
                onTimeOver()
            }
        }
    }
}