package com.minhtu.firesocialmedia.domain.serviceimpl.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.call.CallAction
import com.minhtu.firesocialmedia.utils.AndroidUtils

class CallActionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            CallAction.ACCEPT_CALL_ACTION -> {
                val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                    action = CallAction.ACCEPT_CALL_ACTION
                    putExtra(Constants.KEY_SESSION_ID, intent.getStringExtra(Constants.KEY_SESSION_ID))
                    putExtra(Constants.KEY_CALLEE_ID, intent.getStringExtra(Constants.KEY_CALLEE_ID))
                    putExtra(Constants.FROM_NOTIFICATION, intent.getBooleanExtra(Constants.FROM_NOTIFICATION, false))
                }

                CallSoundManager.stopRingtone()
                AndroidUtils.startForegroundService(context, serviceIntent)
            }

            CallAction.REJECT_CALL_ACTION -> {
                val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                    action = CallAction.REJECT_CALL_ACTION
                    putExtra(Constants.KEY_SESSION_ID, intent.getStringExtra(Constants.KEY_SESSION_ID))
                    putExtra(Constants.KEY_CALLEE_ID, intent.getStringExtra(Constants.KEY_CALLEE_ID))
                }

                CallSoundManager.stopRingtone()
                context.startService(serviceIntent)
            }

            CallAction.STOP_CALL_ACTION_FROM_CALLER -> {
                val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                    action = CallAction.STOP_CALL_ACTION_FROM_CALLER
                    putExtra(Constants.KEY_CALLER_ID, intent.getStringExtra(Constants.KEY_CALLER_ID))
                }

                context.startService(serviceIntent)
            }

            CallAction.STOP_CALL_ACTION_FROM_CALLEE -> {
                val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                    action = CallAction.STOP_CALL_ACTION_FROM_CALLEE
                    putExtra(Constants.KEY_SESSION_ID, intent.getStringExtra(Constants.KEY_SESSION_ID))
                    putExtra(Constants.KEY_CALLEE_ID, intent.getStringExtra(Constants.KEY_CALLEE_ID))
                }

                context.startService(serviceIntent)
            }

            CallAction.REJECT_VIDEO_CALL -> {
                val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                    action = CallAction.REJECT_VIDEO_CALL
                }

                context.startService(serviceIntent)
            }
        }
    }
}