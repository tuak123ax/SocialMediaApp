package com.minhtu.firesocialmedia.domain.serviceimpl.call

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.mapper.call.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDomain
import com.minhtu.firesocialmedia.data.remote.service.call.AudioCallService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.di.AndroidPlatformContext
import com.minhtu.firesocialmedia.di.AppModule
import com.minhtu.firesocialmedia.domain.coordinator.call.CalleeCoordinator
import com.minhtu.firesocialmedia.domain.coordinator.call.CallerCoordinator
import com.minhtu.firesocialmedia.domain.entity.call.AudioCallSession
import com.minhtu.firesocialmedia.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.domain.entity.call.CallEvent
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallNotificationManager.Companion.NOTIF_ID
import com.minhtu.firesocialmedia.domain.serviceimpl.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.domain.serviceimpl.permission.AndroidPermissionManager
import com.minhtu.firesocialmedia.domain.usecases.call.AcceptCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.AddIceCandidatesUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.CalleeUseCases
import com.minhtu.firesocialmedia.domain.usecases.call.CallerUseCases
import com.minhtu.firesocialmedia.domain.usecases.call.CreateOfferUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.EndCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ListenForIncomingCallsUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveAnswer
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveCallStatus
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveIceCandidateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObservePhoneCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.ObserveVideoCall
import com.minhtu.firesocialmedia.domain.usecases.call.SendAnswerUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendIceCandidateUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendOfferUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SendWhoEndCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.SetRemoteDescriptionUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.StartCallUseCase
import com.minhtu.firesocialmedia.domain.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils.Companion.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CallForegroundService : Service() {
    private lateinit var callManager: AudioCallService
    private lateinit var databaseService : DatabaseService
    private lateinit var backgroundScope : CoroutineScope
    private lateinit var callNotificationManager: CallNotificationManager

    private var sessionId = ""
    private var offer : OfferAnswer? = null
    private var callerIdForCalleeFlow : String = ""
    private var calleeIdForCalleeFlow : String = ""
    private var callerFromApp : UserInstance? = null
    private var calleeFromApp : UserInstance? = null
    private var inFlightStartVideoCallSignature: String? = null
    /**
     * SDP hash of the last video offer we processed as callee, used to ignore stale Firebase offers
     * that fire again due to re-subscription (e.g. after Video→Audio navigation).
     */
    private var lastProcessedVideoOfferSdpHash: Int? = null

    private lateinit var initializeCallUseCase: InitializeCallUseCase
    private lateinit var sendSignalingDataUseCase : SendSignalingDataUseCase
    private lateinit var manageCallStateUseCase: ManageCallStateUseCase
    private lateinit var videoCallUseCase: VideoCallUseCase
    private lateinit var callerUseCases : CallerUseCases
    private lateinit var calleeUseCases : CalleeUseCases
    private lateinit var callerCoordinator: CallerCoordinator
    private lateinit var calleeCoordinator : CalleeCoordinator
    private var isStopped : Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        logMessage("onCreate", { "onCreate" })
        super.onCreate()

        //Initialize services
        backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        callNotificationManager = CallNotificationManager(this)
        val platformContext = AndroidPlatformContext(this, AndroidPermissionManager(null))
        val callRepository = AppModule.provideCallRepository(platformContext)
        // Use the same AudioCallService instance across repository and service to avoid leaks
        callManager = platformContext.audioCall
        databaseService = AndroidDatabaseService(applicationContext)
        //Initialize use cases
        initializeCallUseCase = AppModule.provideInitializeCallUseCase(callRepository)
        sendSignalingDataUseCase = AppModule.provideSendSignalingDataUseCase(callRepository)
        manageCallStateUseCase = AppModule.provideManageCallStateUseCase(callRepository)
        videoCallUseCase = AppModule.provideVideoCallUseCase(callRepository)

        //Initialize caller use cases
        callerUseCases = CallerUseCases(
            StartCallUseCase(initializeCallUseCase, sendSignalingDataUseCase, backgroundScope),
            SendOfferUseCase(initializeCallUseCase),
            CreateOfferUseCase(initializeCallUseCase),
            SendIceCandidateUseCase(sendSignalingDataUseCase),
            ObserveIceCandidateUseCase(sendSignalingDataUseCase),
            ObserveAnswer(sendSignalingDataUseCase),
            ObserveCallStatus(sendSignalingDataUseCase),
            ObserveVideoCall(videoCallUseCase),
            EndCallUseCase(manageCallStateUseCase),
            SendWhoEndCallUseCase(manageCallStateUseCase)
        )

        //Initialize callee use cases
        calleeUseCases = CalleeUseCases(
            ListenForIncomingCallsUseCase(initializeCallUseCase),
            ObservePhoneCallUseCase(sendSignalingDataUseCase),
            SendIceCandidateUseCase(sendSignalingDataUseCase),
            AddIceCandidatesUseCase(initializeCallUseCase),
            SendAnswerUseCase(initializeCallUseCase),
            SetRemoteDescriptionUseCase(initializeCallUseCase),
            AcceptCallUseCase(manageCallStateUseCase),
            ObserveVideoCall(videoCallUseCase),
            EndCallUseCase(manageCallStateUseCase),
            SendWhoEndCallUseCase(manageCallStateUseCase)
        )

        //Initialize coordinator which combines all steps into one flow.
        callerCoordinator = CallerCoordinator(
            callerUseCases,
            initializeCallUseCase,
            sendSignalingDataUseCase,
            videoCallUseCase)
        calleeCoordinator = CalleeCoordinator(
            calleeUseCases,
            initializeCallUseCase,
            videoCallUseCase)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logMessage("onStartCommand", { "onStartCommand" })
        if(intent != null) {
            when(intent.action) {
                CallAction.ACCEPT_CALL_ACTION -> {
                    startServiceForCalleeSide(intent)
                }

                CallAction.REJECT_CALL_ACTION -> {
                    rejectCallAction(intent)
                    return START_NOT_STICKY
                }

                CallAction.STOP_CALL_ACTION_FROM_CALLER -> {
                    stopCallActionFromCaller(intent)
                    return START_NOT_STICKY
                }

                CallAction.STOP_CALL_ACTION_FROM_CALLEE -> {
                    stopCallActionFromCallee(intent)
                    return START_NOT_STICKY
                }

                CallAction.START_VIDEO_CALL -> {
                    startVideoCallAction(intent)
                }

                CallAction.REJECT_VIDEO_CALL -> {
                    rejectVideoCallAction()
                }

                else -> {
                    startServiceForCallerSide(intent)
                 }
            }
        }

        return START_STICKY
    }

    private fun startServiceForCallerSide(intent: Intent) {
        if(intent.hasExtra("sessionId") && intent.hasExtra("caller") && intent.hasExtra("callee")) {
            //Start service from caller side
            logMessage("onStartCommand", { "caller side" })
            backgroundScope.launch {
                //Extract data from app.
                val sessionIdFromApp = intent.getStringExtra("sessionId")
                val callerJsonString = intent.getStringExtra("caller")
                val caller = callerJsonString?.let { Json.decodeFromString<UserDTO>(it) }?.toDomain()
                val calleeJsonString = intent.getStringExtra("callee")
                val callee = calleeJsonString?.let { Json.decodeFromString<UserDTO>(it) }?.toDomain()
                if(sessionIdFromApp != null && caller != null && callee != null) {
                    sessionId = sessionIdFromApp
                    callerFromApp = caller
                    calleeFromApp = callee
                    //Create audio call session
                    val audioCallSession = AudioCallSession(
                        sessionId = sessionId,
                        callerId = caller.uid,
                        calleeId = callee.uid,
                        status = CallStatus.RINGING)

                    try{
                        callerCoordinator.startCall(
                            audioCallSession,
                            onSendCallSessionResult = { result ->
                                if(result) {
                                    showCallNotification(callNotificationManager.buildCallNotification(callee.name, caller.uid))
                                    sendNotification("Is calling you", sessionId, caller, callee, "CALL")
                                } else {
                                    logMessage("onSendCallSessionResult", { "send call session fail" })
                                }
                            },
                            onRejectVideoCall = {
                                //Emit event to update UI.
                                CallEventFlow.answerVideoCallState.emit(false)
                            },
                            onAcceptCall = {
                                //Emit event for UI
                                CallEventFlow.events.value = CallEvent.AnswerReceived
                                //Show timer notification
                                showCallNotification(
                                    callNotificationManager.startTimerNotification(
                                        sessionId,
                                        caller.uid,
                                        callee.uid,
                                        true))
                            },
                            onReceiveVideoCall = { videoOffer ->
                                handleIncomingVideoOffer(videoOffer)
                            },
                            onEndCall = {
                                if(!isStopped) {
                                    logMessage("onEndCallCaller", { "caller" })
                                    handleEndCall()
                                    val deleteCallSessionResult = callerUseCases.endCall.invoke(sessionId)
                                    if(deleteCallSessionResult) {
                                        sendEventToUIAfterStopCall(CallEvent.CallEnded)
                                    }
                                    stopService()
                                }
                            }
                        )
                    } catch (ex : Exception) {
                        logMessage("callerCoordinator starts call exception",
                            { ex.message.toString() })
                    }
                }
            }
        }
    }

    private fun rejectVideoCallAction() {
        //Reject video call
        backgroundScope.launch {
            sendSignalingDataUseCase.updateAnswerInFirebase(
                sessionId
            )
        }
    }

    private fun startVideoCallAction(intent: Intent) {
        logMessage("START_VIDEO_CALL", { "START_VIDEO_CALL" })
        backgroundScope.launch {
            val intentSessionId = intent.getStringExtra("sessionId").orEmpty()
            val remoteVideoOfferJsonString = intent.getStringExtra("remoteVideoOffer")
            val currentUserId = intent.getStringExtra("currentUserId").orEmpty()
            val offerHash = remoteVideoOfferJsonString?.hashCode() ?: 0
            val startSignature = "$intentSessionId|$currentUserId|$offerHash"
            if (inFlightStartVideoCallSignature == startSignature) {
                logMessage("START_VIDEO_CALL", { "skip duplicate START_VIDEO_CALL signature=$startSignature" })
                return@launch
            }
            inFlightStartVideoCallSignature = startSignature

            // Keep existing tracks during video rejoin to avoid renderer/sender race conditions.
            // Remote black-screen behavior should come from muted sender frames, not track disposal.
            // Use sessionId from intent when present so we're in sync with the app (avoids stale member after multiple decline/accept)
            intentSessionId.takeIf { it.isNotEmpty() }?.let { sessionIdFromIntent ->
                sessionId = sessionIdFromIntent
            }
            val remoteVideoOffer = remoteVideoOfferJsonString?.let { Json.decodeFromString<OfferAnswerDTO>(it).toDomain() }
            val currentUserIdOrNull = currentUserId.ifEmpty { null }
            try {
                // Remote video offer is null means this is caller side.
                if(remoteVideoOffer == null) {
                    // We're initiating a new video offer: reset the stale-offer guard so that the
                    // other user's fresh video offer (if they also click video call) is not blocked.
                    lastProcessedVideoOfferSdpHash = null
                    // Caller is initiating a fresh video upgrade, so don't apply incoming-offer prep.
                    callerCoordinator.startVideoCall(
                        currentUserIdOrNull,
                        sessionId,
                        onLocalVideoTrackCreated = { localVideoTrack ->
                            //Received local video track
                            //Emit event to update UI
                            CallEventFlow.localVideoTrack.emit(localVideoTrack)
                        },
                        onRejectVideoCall = {
                            // Clean up all video resources so the next attempt starts completely fresh
                            backgroundScope.launch { callManager.stopVideoCallResources() }
                            // Clear video track state for UI
                            CallEventFlow.localVideoTrack.value = null
                            CallEventFlow.remoteVideoTrack.value = null
                            // Callee declined video call — set message for toast and notify UI to navigate back
                            CallEventFlow.videoCallDeclinedMessage.value = calleeFromApp?.name?.let { "$it declined the video call" }
                                ?: "The other person declined the video call"
                            CallEventFlow.answerVideoCallState.value = false
                        })
                } else {
                    callManager.prepareForIncomingVideoNegotiation()
                    calleeCoordinator.startVideoCall(
                        currentUserIdOrNull,
                        sessionId,
                        remoteVideoOffer,
                        onLocalVideoTrackCreated = { localVideoTrack ->
                            //Received local video track
                            //Emit event to update UI
                            CallEventFlow.localVideoTrack.emit(localVideoTrack)
                        }
                    )
                }
            } catch (ex : Exception) {
                val role = if (remoteVideoOffer == null) "caller" else "callee"
                logMessage("${role}Coordinator start video call exception", { ex.message.toString() })
            } finally {
                if (inFlightStartVideoCallSignature == startSignature) {
                    inFlightStartVideoCallSignature = null
                }
            }
        }
    }

    private fun stopCallActionFromCaller(intent: Intent) {
        backgroundScope.launch {
            isStopped = true
            var callerId = ""
            logMessage("STOP_CALL_ACTION_FROM_CALLER", { "STOP_CALL_ACTION_FROM_CALLER" })
            if(callerFromApp != null && calleeFromApp != null) {
                //Send notification to stop service for callee from caller side.
                logMessage("STOP_CALL_ACTION_FROM_CALLER", { "callerId:$callerFromApp" })
                logMessage("STOP_CALL_ACTION_FROM_CALLER", { "calleeId:$calleeFromApp" })
                sendNotification("", sessionId, callerFromApp!!, calleeFromApp!!, "STOP_CALL")
            }
            if(callerFromApp != null) {
                callerId = callerFromApp!!.uid
            } else {
                if(intent.hasExtra(Constants.KEY_CALLER_ID)) {
                    callerId = intent.getStringExtra(Constants.KEY_CALLER_ID).toString()
                }
            }
            val sendWhoEndCall = callerUseCases.sendWhoEndCallUseCase.invoke(sessionId, callerId)
            handleEndCall()
            val deleteCallSessionResult = callerUseCases.endCall.invoke(sessionId)
            if(deleteCallSessionResult) {
                sendEventToUIAfterStopCall(CallEvent.StopCalling)
            }
            stopService()
        }
    }

    private fun stopCallActionFromCallee(intent: Intent) {
        backgroundScope.launch {
            var calleeId = ""
            logMessage("STOP_CALL_ACTION_FROM_CALLEE", { "STOP_CALL_ACTION_FROM_CALLEE" })
            if(intent.hasExtra(Constants.KEY_SESSION_ID)) {
                sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID).toString()
            }
            if(intent.hasExtra(Constants.KEY_CALLEE_ID)) {
                calleeId = intent.getStringExtra(Constants.KEY_CALLEE_ID).toString()
            }
            val sendWhoEndCall = calleeUseCases.sendWhoEndCallUseCase.invoke(sessionId, calleeId)
            handleEndCall()
            val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
            if(deleteCallSessionResult) {
                sendEventToUIAfterStopCall(CallEvent.StopCalling)
            }
            stopService()
        }
    }

    private fun rejectCallAction(intent: Intent){
        backgroundScope.launch {
            logMessage("REJECT_CALL_ACTION", { "REJECT_CALL_ACTION" })
            if(sessionId.isEmpty() && intent.hasExtra(Constants.KEY_SESSION_ID)) {
                sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID).toString()
            }
            if(calleeIdForCalleeFlow.isEmpty() && intent.hasExtra(Constants.KEY_CALLEE_ID)) {
                calleeIdForCalleeFlow = intent.getStringExtra(Constants.KEY_CALLEE_ID).toString()
            }
            val sendWhoEndCall = calleeUseCases.sendWhoEndCallUseCase.invoke(sessionId, calleeIdForCalleeFlow)
            handleEndCall()
            val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
            stopService()
        }
    }

    private suspend fun handleEndCall() {
        //Stop count-up timer.
        logMessage("handleEndCall", { "stopIncomingCallNotification and stopTimerNotificationUpdates" })
        callNotificationManager.stopIncomingCallNotification()
        callNotificationManager.stopTimerNotificationUpdates()
        //Emit event to update UI.
        logMessage("handleEndCall", { "stopCallFlow" })
        releaseCall()
    }

    private suspend fun releaseCall() {
        handleRejectCall()
    }

    private suspend fun stopService() {
        logMessage("handleEndCall", { "Calling stopForeground + stopSelf" })
        withContext(Dispatchers.Main) {
            //Stop foreground service.
            stopForeground(true)
            stopSelf()
        }
    }

    private fun startServiceForCalleeSide(intent : Intent) {
        logMessage("ACCEPT_CALL_ACTION", { "ACCEPT_CALL_ACTION" })
        //Check record audio permission
        if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED) {
            val notificationManager = applicationContext.getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.cancel(NOTIF_ID)
            val calleeIdFromFCM = intent.getStringExtra(Constants.KEY_CALLEE_ID)
            val remoteSessionId = intent.getStringExtra(Constants.KEY_SESSION_ID)
            if(remoteSessionId != null) {
                sessionId = remoteSessionId
            }
            if(calleeIdFromFCM != null) {
                logMessage("onStartCommand", { "callee side" })
                logMessage("onStartCommand", { "sessionId: $sessionId" })
                // Session we are accepting (from Accept button / notification). Only handle this session.
                val acceptedSessionId = sessionId
                //Start service from callee side
                backgroundScope.launch {
                    try{
                        calleeCoordinator.startCall(
                            sessionId,
                            calleeIdFromFCM,
                            onReceivePhoneCallRequest = { callingRequestData ->
                                // Ignore if this is a different call (e.g. stale observer from previous call firing for new session).
                                if (callingRequestData.sessionId != acceptedSessionId) {
                                    logMessage("onReceivePhoneCallRequest", { "ignore session ${callingRequestData.sessionId}, accepted $acceptedSessionId" })
                                } else {
                                    sessionId = callingRequestData.sessionId
                                    offer = callingRequestData.offer
                                    callerIdForCalleeFlow = callingRequestData.callerId
                                    calleeIdForCalleeFlow = callingRequestData.calleeId
                                    //Handle accept call.
                                    handleAcceptCall(callingRequestData)
                                }
                            },
                            onEndCall = {
                                handleEndCall()
                                val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
                                if(deleteCallSessionResult) {
                                    logMessage("DeleteCallSession", { "DeleteCallSession success by callee" })
                                    sendEventToUIAfterStopCall(CallEvent.CallEnded)
                                }
                                stopService()
                            },
                            whoEndCallCallBack = {
                            }
                        )
                    } catch (ex : Exception) {
                        logMessage("calleeCoordinator starts call exception",
                            { ex.message.toString() })
                    }
                }
            }
        } else {
            callNotificationManager.showPermissionNotification()
        }
    }

    private suspend fun handleRejectCall() {
        // Write to Firebase so the other side is notified.
        val sendCallStatusResult = manageCallStateUseCase.rejectCall(sessionId)
        logMessage("handleRejectCall", { "sendCallStatusResult : $sendCallStatusResult" })
        // Stop flow (remove Firebase listener, stop call, cancel notification) so the service is not held by the listener.
        stopCallFlow()
    }

    private suspend fun stopCallFlow() {
        // Remove only the callee-accept observer (observePhoneCallWithoutCheckingInCall) so it doesn't fire for the next call.
        // Do not call stopObservePhoneCall() here — that would also remove the app's incoming-call listener.
        databaseService.stopObservePhoneCallWithoutCheckingInCall()
        //Stop call in call manager
        callManager.stopCall()
        // Clear video/track state so next call starts clean (keep events so UI can show toast)
        CallEventFlow.localVideoTrack.value = null
        CallEventFlow.remoteVideoTrack.value = null
        CallEventFlow.videoCallState.value = null
        CallEventFlow.answerVideoCallState.value = true
        CallEventFlow.videoCallDeclinedMessage.value = null
        lastProcessedVideoOfferSdpHash = null
        //Dismiss notification
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)
    }

    private fun sendEventToUIAfterStopCall(emitEvent: CallEvent?) {
        //Emit event to UI
        //Emit StopVideoCall event first for user who currently is in video call
        CallEventFlow.events.value = CallEvent.StopVideoCall
        if(emitEvent != null && CallEventFlow.events.value != CallEvent.StopCalling &&
            CallEventFlow.events.value != CallEvent.CallEnded) {
            logMessage("handleEndCall", { "send call event to update UI : $emitEvent" })
            CallEventFlow.events.value = emitEvent
        }
    }

    private suspend fun handleAcceptCall(callingRequestData : CallingRequestData) {
        logMessage("handleAcceptCall", { "handleAcceptCall" })
        try{
            calleeCoordinator.acceptCall(
                callingRequestData.sessionId,
                callingRequestData.calleeId,
                callingRequestData.offer!!,
                onAcceptCall = { result ->
                    if(result) {
                        //Show timer notification.
                        showCallNotification(
                            callNotificationManager.startTimerNotification(
                                callingRequestData.sessionId,
                                callingRequestData.callerId,
                                callingRequestData.calleeId,
                                false))
                        //Emit event to update UI.
                        CallEventFlow.events.value = CallEvent.AnswerReceived
                    } else {
                        logMessage("acceptCall", {"accept Call fail"})
                    }
                },
                onReceiveVideoCallRequest = { videoOffer ->
                    handleIncomingVideoOffer(videoOffer)
                }
            )
        } catch (ex : Exception) {
            logMessage("calleeCoordinator accept call exception", { ex.message.toString() })
        }
    }

    private suspend fun handleIncomingVideoOffer(videoOffer: OfferAnswer) {
        val localUserId = when (videoOffer.initiator) {
            callerFromApp?.uid -> calleeFromApp?.uid
            calleeFromApp?.uid -> callerFromApp?.uid
            callerIdForCalleeFlow -> calleeIdForCalleeFlow
            calleeIdForCalleeFlow -> callerIdForCalleeFlow
            else -> null
        }
        logMessage(
            "handleIncomingVideoOffer",
            { "initiator=${videoOffer.initiator}, localUserId=$localUserId, auto=${CallEventFlow.hasAcceptedVideoInCurrentCall.value}" }
        )

        // Ignore self-authored offers; they can still be observed via Firebase updates.
        if (!localUserId.isNullOrEmpty() && localUserId == videoOffer.initiator) {
            logMessage("handleIncomingVideoOffer", { "ignore self-authored offer" })
            return
        }

        // After the first successful video join in this call, subsequent upgrades skip the
        // accept/decline dialog and auto-navigate the UI directly to the VideoCall screen.
        if (CallEventFlow.hasAcceptedVideoInCurrentCall.value &&
            !localUserId.isNullOrEmpty() &&
            sessionId.isNotEmpty()
        ) {
            // Glare guard: if we're currently acting as the video initiator (caller) ourselves,
            // ignore the incoming cross-offer. Processing both simultaneously corrupts PeerConnection state.
            if (inFlightStartVideoCallSignature != null) {
                logMessage("handleIncomingVideoOffer", { "ignore: glare detected — we're currently initiating a video call" })
                return
            }

            val incomingOfferHash = videoOffer.sdp.hashCode()
            // Stale-offer guard: ignore offers we've already processed (Firebase re-fires the current
            // value when the listener re-subscribes, e.g. after Video→Audio navigation).
            if (incomingOfferHash == lastProcessedVideoOfferSdpHash) {
                logMessage("handleIncomingVideoOffer", { "ignore: duplicate stale offer (already processed sdpHash=$incomingOfferHash)" })
                return
            }

            // Emit to videoCallState — the audio screen's LaunchedEffect auto-navigates to VideoCall
            // (because hasAcceptedVideoInCurrentCall=true), and VideoCall's own LaunchedEffect processes
            // the offer as callee. This avoids dual renegotiation (once here, once in the UI).
            logMessage("handleIncomingVideoOffer", { "auto-navigate to video: emit offer to videoCallState" })
            lastProcessedVideoOfferSdpHash = incomingOfferHash
            CallEventFlow.videoCallState.emit(videoOffer)
            return
        }

        // First-time video request still requires UI accept/decline.
        logMessage("handleIncomingVideoOffer", { "emit video offer to UI for accept/decline" })
        callManager.prepareForIncomingVideoNegotiation()
        CallEventFlow.videoCallState.emit(videoOffer)
    }

    override fun onDestroy() {
        callNotificationManager.stopIncomingCallNotification()
        callNotificationManager.stopTimerNotificationUpdates()
        databaseService.stopObservePhoneCallWithoutCheckingInCall()

        backgroundScope.launch(Dispatchers.IO) {
            try { releaseServiceResource() } catch (_: Exception) {}
            try { callManager.releaseResources() } catch (_: Exception) {}

            backgroundScope.cancel() // cancel AFTER cleanup
        }

        super.onDestroy()
    }


    private fun releaseServiceResource() {
        offer = null
        callerFromApp = null
        calleeFromApp = null
    }

    private fun showCallNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }
}