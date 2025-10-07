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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CallForegroundService : Service() {
    private lateinit var callManager: AudioCallService
    private lateinit var databaseService : DatabaseService
    private lateinit var backgroundScope : CoroutineScope
    private lateinit var callNotificationManager: CallNotificationManager

    private var sessionId = ""
    private var offer : OfferAnswer? = null
    private var callerIdForCallee : String = ""
    private var calleeIdForCallee : String = ""
    private var callerFromApp : UserInstance? = null
    private var calleeFromApp : UserInstance? = null

    private lateinit var initializeCallUseCase: InitializeCallUseCase
    private lateinit var sendSignalingDataUseCase : SendSignalingDataUseCase
    private lateinit var manageCallStateUseCase: ManageCallStateUseCase
    private lateinit var videoCallUseCase: VideoCallUseCase
    private lateinit var callerUseCases : CallerUseCases
    private lateinit var calleeUseCases : CalleeUseCases
    private lateinit var callerCoordinator: CallerCoordinator
    private lateinit var calleeCoordinator : CalleeCoordinator

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        logMessage("onCreate", { "onCreate" })
        super.onCreate()

        //Initialize services
        callManager = AndroidAudioCallService(this)
        databaseService = AndroidDatabaseService(this)
        backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        callNotificationManager = CallNotificationManager(this)

        val platformContext = AndroidPlatformContext(this, AndroidPermissionManager(null))
        val callRepository = AppModule.provideCallRepository(platformContext)
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
                                CallEventFlow.videoCallState.emit(videoOffer)
                            },
                            onEndCall = {
                                logMessage("onEndCallCaller", { "caller" })
                                handleEndCall(CallEvent.CallEnded)
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
            val remoteVideoOfferJsonString = intent.getStringExtra("remoteVideoOffer")
            val remoteVideoOffer = remoteVideoOfferJsonString?.let { Json.decodeFromString<OfferAnswerDTO>(it).toDomain() }
            val currentUserId = intent.getStringExtra("currentUserId")
            //Remote video offer is null means this is caller side.
            if(remoteVideoOffer == null) {
                try{
                    callerCoordinator.startVideoCall(
                        currentUserId,
                        sessionId,
                        onLocalVideoTrackCreated = { localVideoTrack ->
                            //Received local video track
                            //Emit event to update UI
                            CallEventFlow.localVideoTrack.emit(localVideoTrack)
                        })
                } catch (ex : Exception) {
                    logMessage("callerCoordinator start video call exception",
                        { ex.message.toString() })
                }
            } else {
                try {
                    calleeCoordinator.startVideoCall(
                        currentUserId,
                        sessionId,
                        remoteVideoOffer,
                        onLocalVideoTrackCreated = { localVideoTrack ->
                            //Received local video track
                            //Emit event to update UI
                            CallEventFlow.localVideoTrack.emit(localVideoTrack)
                        }
                    )
                } catch (ex : Exception) {
                    logMessage("calleeCoordinator start video call exception",
                        { ex.message.toString() })
                }
            }
        }
    }

    private fun stopCallActionFromCaller(intent: Intent) {
        backgroundScope.launch {
            var callerId = ""
            logMessage("STOP_CALL_ACTION_FROM_CALLER", { "STOP_CALL_ACTION_FROM_CALLER" })
            if(callerFromApp != null && calleeFromApp != null) {
                //Send notification to stop service for callee from caller side.
                logMessage("STOP_CALL_ACTION_FROM_CALLER", { "callerId:$callerFromApp" })
                logMessage("STOP_CALL_ACTION_FROM_CALLER", { "calleeId:$calleeFromApp" })
                sendNotification("", sessionId, callerFromApp!!, calleeFromApp!!, "STOP_CALL")
            }
            if(intent.hasExtra(Constants.KEY_CALLER_ID)) {
                callerId = intent.getStringExtra(Constants.KEY_CALLER_ID).toString()
            }
            val sendWhoEndCall = calleeUseCases.sendWhoEndCallUseCase.invoke(sessionId, callerId)
            val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
            handleEndCall(null)
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
            val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
            handleEndCall(CallEvent.StopCalling)
        }
    }

    private fun rejectCallAction(intent: Intent){
        backgroundScope.launch {
            var calleeId = ""
            logMessage("REJECT_CALL_ACTION", { "REJECT_CALL_ACTION" })
            if(intent.hasExtra(Constants.KEY_SESSION_ID)) {
                sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID).toString()
            }
            if(intent.hasExtra(Constants.KEY_CALLEE_ID)) {
                calleeId = intent.getStringExtra(Constants.KEY_CALLEE_ID).toString()
            }
            //Emit stop calling event before end call, so that when homeViewModel observed
            //end call action, it won't emit event again
            CallEventFlow.events.value = CallEvent.StopCalling
            val sendWhoEndCall = calleeUseCases.sendWhoEndCallUseCase.invoke(sessionId, calleeId)
            val deleteCallSessionResult = calleeUseCases.endCallUseCase.invoke(sessionId)
            handleEndCall(null)
        }
    }

    private suspend fun handleEndCall(callEvent : CallEvent?) {
        //Stop count-up timer.
        logMessage("handleEndCall", { "stopTimerNotificationUpdates" })
        callNotificationManager.stopTimerNotificationUpdates()
        //Emit event to update UI.
        logMessage("handleEndCall", { "stopCallFlow" })
        releaseCallAndStopService(callEvent)
    }

    private suspend fun releaseCallAndStopService(callEvent : CallEvent?) {
        handleRejectCall(callEvent)
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
                //Start service from callee side
                backgroundScope.launch {
                    try{
                        calleeCoordinator.startCall(
                            sessionId,
                            calleeIdFromFCM,
                            onReceivePhoneCallRequest = { callingRequestData ->
                                sessionId = callingRequestData.sessionId
                                offer = callingRequestData.offer
                                callerIdForCallee = callingRequestData.callerId
                                calleeIdForCallee = callingRequestData.calleeId
                                //Handle accept call.
                                handleAcceptCall(callingRequestData)
                            },
                            onEndCall = {
                                handleEndCall(CallEvent.CallEnded)
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

    private suspend fun handleRejectCall(emitEvent: CallEvent?) {
        val rejectCallResult = manageCallStateUseCase.rejectCall(sessionId)
        //Stop call flow.
        stopCallFlow(emitEvent)
    }

    private suspend fun stopCallFlow(emitEvent : CallEvent?) {
        //Stop call in call manager
        callManager.stopCall()
        //Emit event to UI
        if(emitEvent != null && CallEventFlow.events.value != CallEvent.StopCalling &&
            CallEventFlow.events.value != CallEvent.CallEnded) {
            CallEventFlow.events.value = emitEvent
        }
        //Dismiss notification
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)
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
                    CallEventFlow.videoCallState.emit(videoOffer)
                }
            )
        } catch (ex : Exception) {
            logMessage("calleeCoordinator accept call exception", { ex.message.toString() })
        }
    }

    override fun onDestroy() {
        // Create a temporary scope just for cleanup
        runBlocking {
            withContext(Dispatchers.IO) {
                try { releaseServiceResource() }
                catch (e: Exception) { logMessage("ReleaseServiceResource") { e.message.toString() } }
                try { callManager.releaseResources() }
                catch (e: Exception) { logMessage("ReleaseCallManager") { e.message.toString() } }
            }
        }
        backgroundScope.cancel()
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