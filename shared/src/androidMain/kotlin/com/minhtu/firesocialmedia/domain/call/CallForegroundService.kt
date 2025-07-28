package com.minhtu.firesocialmedia.domain.call

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
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.CallAction
import com.minhtu.firesocialmedia.data.model.call.CallEvent
import com.minhtu.firesocialmedia.data.model.call.CallEventFlow
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.data.usecases.call.InitializeCallUseCase
import com.minhtu.firesocialmedia.data.usecases.call.ManageCallStateUseCase
import com.minhtu.firesocialmedia.data.usecases.call.SendSignalingDataUseCase
import com.minhtu.firesocialmedia.data.usecases.call.VideoCallUseCase
import com.minhtu.firesocialmedia.domain.AudioCallService
import com.minhtu.firesocialmedia.domain.DatabaseService
import com.minhtu.firesocialmedia.domain.call.CallNotificationManager.Companion.NOTIF_ID
import com.minhtu.firesocialmedia.domain.database.AndroidDatabaseService
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import com.minhtu.firesocialmedia.utils.Utils.Companion.getCallTypeFromSdp
import com.minhtu.firesocialmedia.utils.Utils.Companion.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private var callerIdForCallee : String = ""
    private var calleeIdForCallee : String = ""
    private var callerFromApp : UserInstance? = null
    private var calleeFromApp : UserInstance? = null

    private lateinit var initializeCallUseCase: InitializeCallUseCase
    private lateinit var sendSignalingDataUseCase : SendSignalingDataUseCase
    private lateinit var manageCallStateUseCase: ManageCallStateUseCase
    private lateinit var videoCallUseCase: VideoCallUseCase

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        logMessage("onCreate", { "onCreate" })
        super.onCreate()

        //Initialize services
        callManager = AndroidAudioCallService(this)
        databaseService = AndroidDatabaseService(this)
        backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        callNotificationManager = CallNotificationManager(this)

        //Initialize use cases
        initializeCallUseCase = InitializeCallUseCase(callManager, databaseService, backgroundScope)
        sendSignalingDataUseCase = SendSignalingDataUseCase(callManager, databaseService, backgroundScope)
        manageCallStateUseCase = ManageCallStateUseCase(callManager, databaseService, backgroundScope)
        videoCallUseCase = VideoCallUseCase(callManager, databaseService, backgroundScope)
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
                    stopCallActionFromCaller()
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
                val caller = callerJsonString?.let { Json.decodeFromString<UserInstance>(it) }
                val calleeJsonString = intent.getStringExtra("callee")
                val callee = calleeJsonString?.let { Json.decodeFromString<UserInstance>(it) }
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
                    //Send call session to DB first
                    sendSignalingDataUseCase.sendCallSessionToFirebase(
                        audioCallSession,
                        object : Utils.Companion.BasicCallBack{
                            override fun onSuccess() {
                                //Send call session success
                                showCallNotification(callNotificationManager.buildCallNotification(callee.name))
                            }

                            override fun onFailure() {
                                //Send call session fail
                                logMessage("sendCallSessionToFirebase", {"send call session fail"})
                            }
                        }
                    )
                    //Initialize call service
                    initializeCallUseCase.initializeCall(
                        onInitializeFinished = {
                            backgroundScope.launch {
                                //Create offer
                                initializeCallUseCase.createOffer(
                                    sessionId,
                                    object : Utils.Companion.BasicCallBack{
                                        override fun onSuccess() {
                                            //Send offer success
                                            sendNotification("Is calling you", sessionId, caller, callee, "CALL")
                                        }

                                        override fun onFailure() {
                                            //Send offer fail
                                        }

                                    }
                                )
                            }
                        },
                        onIceCandidateCreated = { iceCandidateData ->
                            backgroundScope.launch {
                                //Send ice candidate to DB after created
                                databaseService.sendIceCandidateToFireBase(
                                    sessionId,
                                    iceCandidateData,
                                    "callerCandidates",
                                    Constants.CALL_PATH,
                                    object : Utils.Companion.BasicCallBack{
                                        override fun onSuccess() {
                                            //Send ice candidate success
                                        }

                                        override fun onFailure() {
                                            //Send ice candidate fail
                                        }

                                    }
                                )
                            }
                        })

                    //Observe ice candidate from callee via DB
                    sendSignalingDataUseCase.observeIceCandidateFromCallee(sessionId)
                    //Observe answer from callee via DB
                    sendSignalingDataUseCase.observeAnswerFromCallee(
                        sessionId,
                        caller.uid,
                        onGetAnswerFromCallee = {
                            logMessage("onGetAnswerFromCallee", { "get answer in service" })
                        },
                        onRejectVideoCall = {
                            //Emit event to update UI.
                            backgroundScope.launch {
                                CallEventFlow.answerVideoCallState.emit(false)
                            }
                        }
                    )
                    //Observe call status via DB
                    sendSignalingDataUseCase.observeCallStatus(
                        sessionId,
                        onAcceptCall = {
                            //Emit event for UI
                            backgroundScope.launch {
                                CallEventFlow.events.emit(CallEvent.AnswerReceived)
                                //Show timer notification
                                showCallNotification(callNotificationManager.startTimerNotification(sessionId, caller.uid, callee.uid))
                                //Start observe video call for caller
                                logMessage("Observer",
                                    { "Start observe video call for caller" })
                                sendSignalingDataUseCase.observeVideoCall(
                                    sessionId,
                                    caller.uid,
                                    onReceiveVideoCallRequest = { videoOffer ->
                                        logMessage("videoCallCallBack", { "emit" })
                                        backgroundScope.launch {
                                            CallEventFlow.videoCallState.emit(videoOffer)
                                        }
                                    }
                                )
                            }
                        },
                        onEndCall = {
                            handleEndCall()
                        }
                    )
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
            val remoteVideoOffer = remoteVideoOfferJsonString?.let { Json.decodeFromString<OfferAnswer>(it) }
            val currentUserId = intent.getStringExtra("currentUserId")
            videoCallUseCase.startVideoCall(
                onLocalVideoTrackCreated = { localVideoTrack ->
                    backgroundScope.launch {
                        //Received local video track
                        //Emit event to update UI
                        CallEventFlow.localVideoTrack.emit(localVideoTrack)

                        //Remote video offer is null means this is caller side.
                        if(remoteVideoOffer == null) {
                            //Create video offer
                            initializeCallUseCase.createVideoOffer(
                                currentUserId,
                                videoOfferCreated = { videoOffer ->
                                    backgroundScope.launch {
                                        //Send offer to DB after created
                                        sendSignalingDataUseCase.sendOfferToFireBase(
                                            sessionId,
                                            videoOffer
                                        )

                                        //Observe answer from callee
                                        sendSignalingDataUseCase.observeAnswerFromCallee(
                                            sessionId,
                                            currentUserId,
                                            onGetAnswerFromCallee = {
                                                //Callee doesn't do anything now
                                            },
                                            onRejectVideoCall = {
                                                //Callee doesn't do anything now
                                            }
                                        )
                                    }
                                })
                        } else {
                            //This is callee side
                            //Set remote description.
                            initializeCallUseCase.setRemoteDescription(remoteVideoOffer)
                            val callType = getCallTypeFromSdp(remoteVideoOffer.sdp)
                            //Create answer
                            initializeCallUseCase.createAnswer(
                                callType,
                                currentUserId,
                                onAnswerCreated = { answer ->
                                    backgroundScope.launch {
                                        //Send answer to DB.
                                        sendSignalingDataUseCase.sendAnswerToFirebase(
                                            sessionId,
                                            answer
                                        )
                                    }
                                })
                        }
                    }
                })
        }
    }

    private fun stopCallActionFromCaller() {
        backgroundScope.launch {
            logMessage("STOP_CALL_ACTION_FROM_CALLER", { "STOP_CALL_ACTION_FROM_CALLER" })
            if(callerFromApp != null && calleeFromApp != null) {
                //Send notification to stop service for callee from caller side.
                sendNotification("", sessionId, callerFromApp!!, calleeFromApp!!, "STOP_CALL")
            }
            //Handle reject call action.
            handleRejectCall(CallEvent.StopCalling)
            withContext(Dispatchers.IO) {
                //Release call manager resources
                callManager.releaseResources()
            }
            withContext(Dispatchers.Main) {
                //Stop foreground service.
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun rejectCallAction(intent : Intent){
        backgroundScope.launch {
            logMessage("REJECT_CALL_ACTION", { "REJECT_CALL_ACTION" })
            val remoteSessionId = intent.getStringExtra(Constants.KEY_SESSION_ID)
            if(remoteSessionId != null) {
                sessionId = remoteSessionId
            }
            //Handle reject call action.
            handleRejectCall(CallEvent.CallEnded)
            withContext(Dispatchers.IO) {
                //Release call manager resources
                callManager.releaseResources()
            }
            withContext(Dispatchers.Main) {
                //Stop foreground service.
                stopForeground(true)
                stopSelf()
            }
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
                    logMessage("initialize", { "start initialize" })
                    //Initialize call service
                    initializeCallUseCase.initializeCall(
                        onInitializeFinished = {
                            backgroundScope.launch {
                                logMessage("observePhoneCallWithoutCheckingInCall", { "start observe" })
                                //Observe phone call request.
                                sendSignalingDataUseCase.observePhoneCallWithoutCheckingInCall(
                                    calleeIdFromFCM,
                                    onReceivePhoneCallRequest = { remoteSessionId, remoteOffer, remoteCallerId, remoteCalleeId ->
                                        sessionId = remoteSessionId
                                        offer = remoteOffer
                                        callerIdForCallee = remoteCallerId
                                        calleeIdForCallee = remoteCalleeId
                                        //Handle accept call.
                                        handleAcceptCall(sessionId, offer!!, callerIdForCallee, calleeIdForCallee)
                                    },
                                    onEndCall = {
                                        handleEndCall()
                                    }
                                )
                            }
                        },
                        onIceCandidateCreated = { iceCandidateData ->
                            backgroundScope.launch {
                                //Send ice candidate to DB after created
                                databaseService.sendIceCandidateToFireBase(
                                    sessionId,
                                    iceCandidateData,
                                    "calleeCandidates",
                                    Constants.CALL_PATH,
                                    object : Utils.Companion.BasicCallBack{
                                        override fun onSuccess() {
                                            //Send ice candidate success
                                        }

                                        override fun onFailure() {
                                            //Send ice candidate fail
                                        }

                                    }
                                )
                            }
                        })
                }
            }
        } else {
            callNotificationManager.showPermissionNotification()
        }
    }

    private fun handleRejectCall(emitEvent: CallEvent) {
        backgroundScope.launch {
            manageCallStateUseCase.rejectCall(sessionId)
            //Stop call flow.
            stopCallFlow(emitEvent)
        }
    }

    private fun handleEndCall() {
        backgroundScope.launch {
            manageCallStateUseCase.endCall(sessionId)
            //Stop count-up timer.
            logMessage("handleEndCall", { "stopTimerNotificationUpdates" })
            callNotificationManager.stopTimerNotificationUpdates()
            //Emit event to update UI.
            logMessage("handleEndCall", { "stopCallFlow" })
            stopCallFlow(CallEvent.CallEnded)
            withContext(Dispatchers.IO) {
                //Release call manager resources
                callManager.releaseResources()
            }
            logMessage("handleEndCall", { "Calling stopForeground + stopSelf" })
            withContext(Dispatchers.Main) {
                //Stop foreground service.
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private suspend fun stopCallFlow(emitEvent : CallEvent) {
        //Stop call in call manager
        callManager.stopCall()
        //Emit event to UI
        CallEventFlow.events.emit(emitEvent)
        //Dismiss notification
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)
    }

    private fun handleAcceptCall(sessionId : String,
                                 offer : OfferAnswer,
                                 callerId : String,
                                 calleeId : String) {
        logMessage("handleAcceptCall", { "handleAcceptCall" })
        backgroundScope.launch{
            //Send callee data to DB.
            sendCalleeData(sessionId, offer)
            //Accept call
            manageCallStateUseCase.acceptCall(
                sessionId,
                object : Utils.Companion.BasicCallBack{
                    override fun onSuccess() {
                        //Show timer notification.
                        showCallNotification(callNotificationManager.startTimerNotification(sessionId, callerId, calleeId))
                        //Emit event to update UI.
                        backgroundScope.launch {
                            CallEventFlow.events.emit(CallEvent.AnswerReceived)
                            //Start observe video call for callee
                            logMessage("Observer", { "Start observe video call for callee" })
                            videoCallUseCase.observeVideoCall(
                                sessionId,
                                calleeId,
                                onReceivedVideoCall = { videoOffer ->
                                    backgroundScope.launch {
                                        CallEventFlow.videoCallState.emit(videoOffer)
                                    }
                                }
                            )
                        }
                    }

                    override fun onFailure() {
                        logMessage("acceptCall", {"accept Call fail"})
                    }

                })
        }
    }

    private suspend fun sendCalleeData(
        sessionId: String,
        offer: OfferAnswer
    ) {
        logMessage("sendCalleeData", { "sendCalleeData: $sessionId" })
        //Set remote description.
        initializeCallUseCase.setRemoteDescription(offer)
        val callType = getCallTypeFromSdp(offer.sdp)
        //Create answer.
        initializeCallUseCase.createAnswer(
            callType,
            null,
            onAnswerCreated = { answer ->
                backgroundScope.launch {
                    //Send answer to DB after created.
                    sendSignalingDataUseCase.sendAnswerToFirebase(
                        sessionId,
                        answer
                    )
                }
            }
        )
    }

    override fun onDestroy() {
        //Release call resources.
        backgroundScope.launch {
            try{
                //Release service resources.
                releaseServiceResource()
            } catch (e : Exception) {
                logMessage("Exception when releasing resources", {e.message.toString()} )
            }
            try{
                callManager.releaseResources()
            } catch (e : Exception) {
                logMessage("Exception when trying releasing call resources", {e.message.toString()})
            }
        }
        super.onDestroy()
    }

    private fun releaseServiceResource() {
        offer = null
        callerFromApp = null
        calleeFromApp = null
    }

    private fun showCallNotification(notification: Notification) {
        logMessage("showCallNotification", {"startForeground"})
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