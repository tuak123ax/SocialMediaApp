package com.minhtu.firesocialmedia.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import com.google.firebase.database.FirebaseDatabase
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.initPlatformContext

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initPlatformContext(this)
        SupabaseStorageHelper.initExtensionCache(this)
        createChannelNotification()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
    private fun createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                "Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}