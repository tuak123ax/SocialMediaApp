package com.minhtu.firesocialmedia.android.incomingcall

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.minhtu.firesocialmedia.android.constants.AppConstants
import com.minhtu.firesocialmedia.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.android.service.serviceimpl.call.CallActionBroadcastReceiver
import com.minhtu.firesocialmedia.android.service.serviceimpl.call.CallNotificationManager.Companion.NOTIF_ID
import com.minhtu.firesocialmedia.android.service.serviceimpl.call.CallSoundManager
import com.minhtu.firesocialmedia.ui.theme.FireSocialMediaCommonTheme

/**
 * Transparent, over-lock-screen activity whose sole purpose is to request the
 * RECORD_AUDIO permission *before* the incoming call screen is shown.
 *
 * Why a separate activity instead of requesting inside IncomingCallActivity?
 * The system permission dialog (com.android.permissioncontroller) is a separate
 * process/task and does NOT carry showWhenLocked. When launched from an activity
 * that is already showing over the lock screen, Android promotes the dialog to
 * also appear over the lock screen — but only if the launching activity is the
 * foreground window at the time of the request. Because IncomingCallActivity
 * shows the call UI first and then requests on Accept, there is no guarantee
 * the permission dialog will render above the keyguard.
 *
 * This activity is launched *instead* of IncomingCallActivity when permission is
 * missing. It immediately fires the system permission dialog. On grant it hands
 * off to IncomingCallActivity; on permanent denial it shows an in-UI dialog
 * directing the user to Settings.
 *
 * Flow:
 *  Permission missing
 *    └─ IncomingCallWakeService → AudioPermissionOverLockScreenActivity
 *         ├─ granted  → IncomingCallActivity (normal flow)
 *         ├─ denied once  → show RATIONALE dialog
 *         │     ├─ "Grant Permission" → re-request
 *         │     └─ "Decline Call"     → reject + finish
 *         └─ permanently denied → show SETTINGS dialog
 *               ├─ "Open Settings" → system app settings (user fixes, returns, taps Accept)
 *               └─ "Decline Call"  → reject + finish
 */
class AudioPermissionOverLockScreenActivity : ComponentActivity() {

    private enum class DialogState { RATIONALE, SETTINGS }

    private val dialogState = mutableStateOf<DialogState?>(null)

    // Call data passed through from IncomingCallWakeService
    private var sessionId: String? = null
    private var calleeId: String? = null
    private var callerName: String = ""
    private var callerAvatar: String = ""

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchIncomingCallActivity()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                // Denied once — can ask again; show rationale.
                dialogState.value = DialogState.RATIONALE
            } else {
                // Permanently denied — direct to Settings.
                dialogState.value = DialogState.SETTINGS
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyOverLockScreenFlags()

        sessionId = intent.getStringExtra(AppConstants.KEY_SESSION_ID)
        calleeId = intent.getStringExtra(AppConstants.KEY_CALLEE_ID)
        callerName = intent.getStringExtra(AppConstants.KEY_CALLER_NAME).orEmpty()
        callerAvatar = intent.getStringExtra(AppConstants.KEY_CALLER_AVATAR).orEmpty()

        // If permission was granted between the service check and our onCreate
        // (extremely rare but possible), go straight to the call screen.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchIncomingCallActivity()
            return
        }

        // Render a transparent background. The only visible content will be the dialogs.
        setContent {
            FireSocialMediaCommonTheme {
                val state by dialogState
                when (state) {
                    DialogState.RATIONALE -> {
                        AlertDialog(
                            onDismissRequest = {
                                // Dismissed without choosing — user can still decide via the
                                // call notification; do not reject automatically.
                                dialogState.value = null
                            },
                            title = { Text("Microphone Permission Required") },
                            text = {
                                Text(
                                    "This app needs access to your microphone to make audio calls. " +
                                    "Please grant the permission to continue."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    dialogState.value = null
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }) { Text("Grant Permission") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    dialogState.value = null
                                    rejectAndFinish()
                                }) { Text("Decline Call") }
                            }
                        )
                    }

                    DialogState.SETTINGS -> {
                        AlertDialog(
                            onDismissRequest = {
                                dialogState.value = null
                            },
                            title = { Text("Microphone Permission Denied") },
                            text = {
                                Text(
                                    "Microphone permission has been permanently denied. " +
                                    "Please go to Settings → Permissions → Microphone and enable it, " +
                                    "then come back to accept the call."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    dialogState.value = null
                                    openAppSettings()
                                    // Finish this activity — the call notification remains so the
                                    // user can tap it again after granting from Settings.
                                    finish()
                                }) { Text("Open Settings") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    dialogState.value = null
                                    rejectAndFinish()
                                }) { Text("Decline Call") }
                            }
                        )
                    }

                    null -> { /* Transparent — only reached briefly before dialog shows */ }
                }
            }
        }

        // Immediately fire the system permission dialog. It will appear over the lock screen
        // because this activity already has the over-lock-screen window token.
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    /** Hands off to IncomingCallActivity with the full call data and exits. */
    private fun launchIncomingCallActivity() {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra(AppConstants.KEY_SESSION_ID, sessionId)
            putExtra(AppConstants.KEY_CALLEE_ID, calleeId)
            putExtra(AppConstants.KEY_CALLER_NAME, callerName)
            putExtra(AppConstants.KEY_CALLER_AVATAR, callerAvatar)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Sends a REJECT broadcast so the caller is notified and the Firebase session is
     * cleaned up. Called only when the user explicitly taps "Decline Call".
     */
    private fun rejectAndFinish() {
        val broadcastIntent = Intent(this, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.REJECT_CALL_ACTION
            putExtra(AppConstants.KEY_SESSION_ID, sessionId)
            putExtra(AppConstants.KEY_CALLEE_ID, calleeId)
        }
        sendBroadcast(broadcastIntent)
        CallSoundManager.stopRingtone()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
        finish()
    }

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun applyOverLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}

