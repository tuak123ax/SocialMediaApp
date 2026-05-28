package com.minhtu.firesocialmedia.android.incomingcall

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.core.constants.Constants

/**
 * A short-lived foreground service (foregroundServiceType="shortService") that directly starts
 * IncomingCallActivity on Android 14+.
 *
 * On Android 14, USE_FULL_SCREEN_INTENT is restricted for non-dialer apps — the system only
 * shows a HUD banner, it does NOT launch the activity. The correct workaround is to call
 * startActivity() directly from a running foreground service, which Android 14 permits when
 * the service type is "shortService".
 *
 * Flow:
 *  1. startForeground() → service is now "running" in foreground (required for startActivity privilege)
 *  2. startActivity() → launches IncomingCallActivity directly over the lock screen
 *  3. stopSelf() → service exits; activity keeps the wake lock alive
 */
class IncomingCallWakeService : Service() {

    companion object {
        private const val WAKE_CHANNEL_ID = "incoming_call_wake_channel"
        private const val WAKE_NOTIF_ID = 9_002

        fun buildStartIntent(
            context: android.content.Context,
            sessionId: String?,
            calleeId: String?,
            callerName: String,
            callerAvatar: String
        ): Intent = Intent(context, IncomingCallWakeService::class.java).apply {
            putExtra(Constants.KEY_SESSION_ID, sessionId)
            putExtra(Constants.KEY_CALLEE_ID, calleeId)
            putExtra(Constants.KEY_CALLER_NAME, callerName)
            putExtra(Constants.KEY_CALLER_AVATAR, callerAvatar)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra(Constants.KEY_SESSION_ID)
        val calleeId = intent?.getStringExtra(Constants.KEY_CALLEE_ID)
        val callerName = intent?.getStringExtra(Constants.KEY_CALLER_NAME).orEmpty()
        val callerAvatar = intent?.getStringExtra(Constants.KEY_CALLER_AVATAR).orEmpty()

        ensureChannel()

        // Decide which activity to launch:
        // - If RECORD_AUDIO is already granted → go straight to IncomingCallActivity.
        // - If not → go to AudioPermissionOverLockScreenActivity first. That activity:
        //     • carries showWhenLocked so the system permission dialog renders over the keyguard
        //     • on grant, forwards to IncomingCallActivity
        //     • on permanent denial, shows a Settings-redirect dialog
        //   This is the only reliable way to show the permission dialog over the lock screen,
        //   because the system permissioncontroller dialog inherits the over-lock-screen token
        //   from the activity that launched it.
        val audioPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val targetActivityClass = if (audioPermissionGranted) {
            IncomingCallActivity::class.java
        } else {
            AudioPermissionOverLockScreenActivity::class.java
        }

        // Build a tap-to-open PendingIntent for the notification banner (used on all API levels)
        val tapIntent = Intent(this, targetActivityClass).apply {
            putExtra(Constants.KEY_SESSION_ID, sessionId)
            putExtra(Constants.KEY_CALLEE_ID, calleeId)
            putExtra(Constants.KEY_CALLER_NAME, callerName)
            putExtra(Constants.KEY_CALLER_AVATAR, callerAvatar)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            this,
            200,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, WAKE_CHANNEL_ID)
            .setContentTitle("Incoming Call")
            .setContentText(if (callerName.isNotBlank()) "$callerName is calling you" else "Incoming call")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            // Keep setFullScreenIntent as fallback for devices that honour it
            .setFullScreenIntent(tapPendingIntent, true)
            .build()

        // Step 1: promote to foreground — this grants the BAL (Background Activity Launch)
        // privilege needed to call startActivity() on Android 10+
        startForegroundCompat(notification)

        // Step 2: directly launch the activity.
        // - On Android 14+ the full-screen intent is suppressed for non-dialer apps, but
        //   startActivity() from a running foreground service IS allowed.
        // - On older versions this is also fine: the full-screen intent may already have fired,
        //   but FLAG_ACTIVITY_SINGLE_TOP / singleTask launch mode prevents double-open.
        startActivity(tapIntent)

        // Step 3: exit immediately — shortService contract.
        stopSelf()
        return START_NOT_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                WAKE_NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            )
        } else {
            startForeground(WAKE_NOTIF_ID, notification)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WAKE_CHANNEL_ID,
                "Incoming Call Wake",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(false)
                vibrationPattern = longArrayOf(0L)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}


