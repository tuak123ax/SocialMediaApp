package com.minhtu.firesocialmedia.android

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.platform.TokenStorage.updateTokenInStorage
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.UserInstance
import java.io.IOException
import java.net.URL

class AppFirebaseNotificationService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "📩 Message received!")

        // 🔹 Retrieve Data Payload
        val user : UserInstance?
        if (message.data.isNotEmpty()) {
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
}