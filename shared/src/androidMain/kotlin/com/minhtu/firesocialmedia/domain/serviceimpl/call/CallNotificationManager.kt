package com.minhtu.firesocialmedia.domain.serviceimpl.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.domain.entity.call.CallEventFlow
import com.minhtu.firesocialmedia.utils.PermissionRequestActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CallNotificationManager(private val context: Context) {
    companion object {
        const val NOTIF_ID = 9999
        const val channelId = "call_channel"
        const val timerChannelId = "timer_channel"
        const val PERMISSION_ID = 8888

        /** Global timer job so any instance can cancel the previous call's timer when starting a new call or when the service is recreated. */
        private var currentTimerJob: Job? = null

        internal fun cancelTimerJob() {
            currentTimerJob?.cancel()
            currentTimerJob = null
            CallEventFlow.callDurationSeconds.value = 0
        }
    }

    fun startTimerNotification(
        sessionId: String,
        callerId: String,
        calleeId: String,
        isCaller : Boolean
    ): Notification {
        // Stop any previous call's timer (e.g. from a prior call when service was recreated or when starting a new call in same instance).
        cancelTimerJob()

        var seconds = 0
        CallEventFlow.callDurationSeconds.value = 0

        val stopIntent = Intent(context, CallActionBroadcastReceiver::class.java).apply {
            action = if(isCaller) CallAction.STOP_CALL_ACTION_FROM_CALLER
            else CallAction.REJECT_CALL_ACTION
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Activity intent to open new activity for granting permission.
        val activityIntent = Intent().apply {
            component = ComponentName(context.packageName, "com.minhtu.firesocialmedia.android.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.FROM_NOTIFICATION, true)
            putExtra("sessionId", sessionId)
            putExtra("callerId", callerId)
            putExtra("calleeId", calleeId)
        }

        val callPendingIntent = PendingIntent.getActivity(
            context,
            4,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Start timer to update the notification every second; also push to CallEventFlow so UI stays in sync
        currentTimerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val timeText = String.format("Call in progress: %02d:%02d", seconds / 60, seconds % 60)

                val updatedNotification = buildTimerNotification(timeText, stopPendingIntent, callPendingIntent)

                context.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, updatedNotification)

                seconds++
                CallEventFlow.callDurationSeconds.value = seconds
                delay(1000L)
            }
        }

        // Build and return the **initial** notification immediately
        val initialText = String.format("Call in progress: %02d:%02d", seconds / 60, seconds % 60)
        return buildTimerNotification(initialText, stopPendingIntent, callPendingIntent)
    }

    private fun buildTimerNotification(
        timeText: String,
        stopPendingIntent: PendingIntent,
        callPendingIntent: PendingIntent
    ): Notification {
        val channelName = "Timer Call Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(timerChannelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(false)
                vibrationPattern = longArrayOf(0L)
                setSound(null, null)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(context, timerChannelId)
            .setContentTitle("Call Active")
            .setVibrate(null)
            .setSound(null)
            .setContentText(timeText)
            .setSmallIcon(R.drawable.notification)
            .addAction(R.drawable.ic_reject_call, "Stop", stopPendingIntent)
            .setContentIntent(callPendingIntent)
            .setOngoing(true)
            .build()
    }

    fun stopTimerNotificationUpdates() {
        cancelTimerJob()
    }
    fun stopIncomingCallNotification() {
        CallSoundManager.stopRingtone()
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)
    }

    fun showPermissionNotification() {
        val requestPermissionIntent = Intent(context, PermissionRequestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val requestPermissionPendingIntent = PendingIntent.getActivity(
            context,
            3,
            requestPermissionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Permission is missing")
            .setContentText("Please grant permission to use audio call feature!")
            .setSmallIcon(R.drawable.notification)
            .setAutoCancel(true)
            .setContentIntent(requestPermissionPendingIntent)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(PERMISSION_ID, notification)
    }

    fun buildCallNotification(calleeName: String, callerId: String): Notification {
        // Channel ID changed from "call_channel_v2" to "call_channel_v3" (silent).
        // Reason: NotificationChannels cannot be mutated after creation — the old channel had
        // setSound(defaultRingtone) which made the OS play the default ring simultaneously with
        // our custom MediaPlayer ringtone (CallSoundManager.playRingtoneForCaller).
        // The new channel has no sound; CallSoundManager is solely responsible for audio.
        val callerChannelId = "call_channel_v3"
        val channelName = "Call Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                callerChannelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(false)
                vibrationPattern = longArrayOf(0L)
            }

            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val rejectIntent = Intent(context, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.STOP_CALL_ACTION_FROM_CALLER
            putExtra(Constants.KEY_CALLER_ID, callerId)
        }

        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, callerChannelId)
            .setContentTitle("Calling")
            .setContentText("You are calling $calleeName")
            .setSmallIcon(R.drawable.notification)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_reject_call, "Stop", rejectPendingIntent)
            .build()
    }
}