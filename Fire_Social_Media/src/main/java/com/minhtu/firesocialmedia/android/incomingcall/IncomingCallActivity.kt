package com.minhtu.firesocialmedia.android.incomingcall

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.core.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallActionBroadcastReceiver
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallNotificationManager.Companion.NOTIF_ID
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallSoundManager
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.presentation.calling.incomingcall.IncomingCallScreen
import com.minhtu.firesocialmedia.ui.theme.FireSocialMediaCommonTheme
import com.seiko.imageloader.LocalImageLoader

/**
 * Displays the incoming call UI (accept / decline) over the lock screen.
 *
 * By the time this activity is started, RECORD_AUDIO permission is guaranteed to be
 * granted — either it was already granted, or AudioPermissionOverLockScreenActivity
 * obtained it first. No permission handling is needed here.
 */
class IncomingCallActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acquireWakeLock()
        applyWakeAndLockScreenFlags()

        val sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID)
        val calleeId = intent.getStringExtra(Constants.KEY_CALLEE_ID)
        val callerName = intent.getStringExtra(Constants.KEY_CALLER_NAME).orEmpty()
        val callerAvatar = intent.getStringExtra(Constants.KEY_CALLER_AVATAR).orEmpty()

        setContent {
            FireSocialMediaCommonTheme {
                val localImageLoaderValue = LocalImageLoader provides remember { generateImageLoader() }
                IncomingCallScreen(
                    callerName = callerName,
                    callerAvatar = callerAvatar,
                    localImageLoaderValue = localImageLoaderValue,
                    onAccept = {
                        handleCallAction(
                            action = CallAction.ACCEPT_CALL_ACTION,
                            sessionId = sessionId,
                            calleeId = calleeId,
                            fromNotification = true
                        )
                    },
                    onReject = {
                        handleCallAction(
                            action = CallAction.REJECT_CALL_ACTION,
                            sessionId = sessionId,
                            calleeId = calleeId,
                            fromNotification = false
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
            "FireSocialMedia:IncomingCallWakeLock"
        ).also {
            it.acquire(60_000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun applyWakeAndLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun handleCallAction(
        action: String,
        sessionId: String?,
        calleeId: String?,
        fromNotification: Boolean
    ) {
        val intent = Intent(this, CallActionBroadcastReceiver::class.java).apply {
            this.action = action
            putExtra(Constants.KEY_SESSION_ID, sessionId)
            putExtra(Constants.KEY_CALLEE_ID, calleeId)
            if (fromNotification) {
                putExtra(Constants.FROM_NOTIFICATION, true)
            }
        }
        sendBroadcast(intent)

        CallSoundManager.stopRingtone()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)

        finishAndRemoveTask()
    }
}