package com.minhtu.firesocialmedia.services.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.minhtu.firesocialmedia.MainActivity
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL
import java.util.HashMap

class AppFirebaseNotificationService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val userId = message.getData()[Constants.KEY_USER_ID]
        val name = message.getData()[Constants.KEY_NAME]
        val avatar = message.getData()[Constants.KEY_AVATAR]
        val fcm_token = message.getData()[Constants.KEY_FCM_TOKEN]
        val email = message.getData()[Constants.KEY_EMAIL]
        val notificationMessage = message.getData()[Constants.KEY_MESSAGE]
        val user = UserInstance(email!!,avatar!!,name!!,"",fcm_token!!,userId!!, HashMap())
        sendNotification(user, notificationMessage)
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
        val receiverData = Bundle()
        receiverData.putSerializable("receiverUser", user)
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
        updateToken(token)
        super.onNewToken(token)
    }

    private fun updateToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val secureSharedPreferences = CryptoHelper.getEncryptedSharedPreferences(applicationContext)
            secureSharedPreferences.edit().putString(Constants.KEY_FCM_TOKEN, token).apply()
        }
    }
}