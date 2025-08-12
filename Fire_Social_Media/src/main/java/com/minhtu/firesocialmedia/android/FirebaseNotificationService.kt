package com.minhtu.firesocialmedia.android

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.CallAction
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallActionBroadcastReceiver
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallNotificationManager.Companion.NOTIF_ID
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallNotificationManager.Companion.channelId
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallSoundManager
import com.minhtu.firesocialmedia.platform.TokenStorage.updateTokenInStorage
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class AppFirebaseNotificationService: FirebaseMessagingService() {
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "📩 Message received!")

        // 🔹 Retrieve Data Payload
        val user : UserInstance?
        if (message.data.isNotEmpty()) {
            val notificationType = message.data[Constants.REMOTE_MSG_TYPE]
            logMessage("notificationType", {"$notificationType"})
            //Notification of foreground service to make audio call
            when(notificationType) {
                "CALL" -> {
                    //Extract data
                    val sessionId = message.data[Constants.KEY_SESSION_ID]
                    val callerName = message.data[Constants.KEY_CALLER_NAME]
                    val callerAvatar = message.data[Constants.KEY_CALLER_AVATAR]
                    val callerId = message.data[Constants.KEY_CALLER_ID]
                    val calleeName = message.data[Constants.KEY_CALLEE_NAME]
                    val calleeAvatar = message.data[Constants.KEY_CALLEE_AVATAR]
                    val calleeId = message.data[Constants.KEY_CALLEE_ID]

                    if(sessionId != null && calleeId != null && callerName != null && callerAvatar != null) {
                        buildCallNotification(sessionId, calleeId, callerName, callerAvatar)
                        CallSoundManager.playRingtone(applicationContext)
                    }
                }
                "STOP_CALL" -> {
                    CallSoundManager.stopRingtone()
                    val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(NOTIF_ID)
                }
                else -> {
                    //Normal notification
                    val fcmToken = message.data[Constants.KEY_FCM_TOKEN]
                    val userId = message.data[Constants.KEY_USER_ID]
                    val avatar = message.data[Constants.KEY_AVATAR]
                    val email = message.data[Constants.KEY_EMAIL]

                    Log.d("FCM", "📊 Data Payload:")
                    Log.d("FCM", "🔹 fcm_token: $fcmToken")
                    Log.d("FCM", "🔹 user_id: $userId")
                    Log.d("FCM", "🔹 avatar: $avatar")
                    Log.d("FCM", "🔹 email: $email")

                    val title = message.data[Constants.REMOTE_MSG_TITLE]
                    val body = message.data[Constants.REMOTE_MSG_BODY]
                    Log.d("FCM", "🔹 title: $title")
                    Log.d("FCM", "🔹 body: $body")
                    user = UserInstance(email!!, avatar!!,title!!,"",fcmToken!!,userId!!, HashMap())
                    sendNotification(user, body)
                }
            }
        }

    }

    private fun sendNotification(user: UserInstance, content: String?) {
        var bitmap: Bitmap? = null
        try {
            val url = URL(user.image)
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val intent = Intent(this, MainActivity::class.java)
        val gson = Gson()
        val jsonUser = gson.toJson(user)
        val receiverData = Bundle()
        receiverData.putSerializable("receiverUser", jsonUser)
        intent.putExtras(receiverData)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(user.name)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
        }
        val notification = builder.build()
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    override fun onNewToken(token: String) {
        Log.e("onNewToken", token)
        updateTokenInStorage(token)
        super.onNewToken(token)
    }

    private fun buildCallNotification(sessionId : String,
                                      calleeId : String,
                                      callerName : String,
                                      callerAvatar : String) {
        logMessage("buildCallNotification", { "buildCallNotification" })
        logMessage("buildCallNotification", { "callerAvatar: $callerAvatar" })
        val channelName = "Call Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(false)
                vibrationPattern = longArrayOf(0L)
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val acceptIntent = Intent(this, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.ACCEPT_CALL_ACTION
            putExtra(Constants.KEY_SESSION_ID, sessionId)
            putExtra(Constants.KEY_CALLEE_ID, calleeId)
            putExtra(Constants.FROM_NOTIFICATION, true)
        }

        val acceptPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(this, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.REJECT_CALL_ACTION
            putExtra(Constants.KEY_SESSION_ID, sessionId)
        }

        val rejectPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Custom view for expand notification
        val expandRemoteViews = RemoteViews(packageName, R.layout.notification_call_expand)
        //Set name and avatar on custom view
        expandRemoteViews.setTextViewText(R.id.text_caller_name, callerName)
        //Setup click on custom view
        expandRemoteViews.setOnClickPendingIntent(R.id.btn_accept, acceptPendingIntent)
        expandRemoteViews.setOnClickPendingIntent(R.id.btn_reject, rejectPendingIntent)

        //Custom view for collapse notification
        val collapseRemoteViews = RemoteViews(packageName, R.layout.notification_call_collapse)
        //Set name and avatar on custom view
        collapseRemoteViews.setTextViewText(R.id.text_caller_name_collapse, callerName)
        //Setup click on custom view
        collapseRemoteViews.setOnClickPendingIntent(R.id.btn_accept_collapse, acceptPendingIntent)
        collapseRemoteViews.setOnClickPendingIntent(R.id.btn_reject_collapse, rejectPendingIntent)

        notificationScope.launch {
            val result = runCatching {
                applicationContext.imageLoader
                    .execute(
                        ImageRequest.Builder(applicationContext)
                            .data(callerAvatar)
                            .build()
                    )
                    .drawable
                    ?.toBitmap()
            }
            val fallbackBitmap = BitmapFactory.decodeResource(resources, R.drawable.unknownavatar)
            val finalBitmap = result.getOrNull() ?: fallbackBitmap

            expandRemoteViews.setImageViewBitmap(R.id.image_caller, finalBitmap)
            collapseRemoteViews.setImageViewBitmap(R.id.image_caller_collapse, finalBitmap)

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle("Incoming Call")
                .setContentText("You have a new call from $callerName")
                .setSmallIcon(R.drawable.notification)
                .setCustomContentView(collapseRemoteViews)
                .setCustomBigContentView(expandRemoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // triggers heads-up
                .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // required for custom view
                .setOngoing(true)
                .build()

            withContext(Dispatchers.Main) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notification)
                } else {
                    showToast("Notification permission is not granted!")
                }
            }
        }
    }
}