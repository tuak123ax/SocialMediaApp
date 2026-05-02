package com.minhtu.firesocialmedia.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.initPlatformContext
import java.util.concurrent.Executors

private const val APP_PACKAGE = "com.minhtu.firesocialmedia"
private const val TAG = "StrictMode"

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initPlatformContext(this)
        SupabaseStorageHelper.initExtensionCache(this)
        createChannelNotification()
        setupStrictMode()
        setupLogging()
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

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setupStrictModeApi28()
            } else {
                setupStrictModeLegacy()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupStrictModeApi28() {
        val executor = Executors.newSingleThreadExecutor()

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads()
                .penaltyListener(executor) { violation ->
                    if (violation.stackTrace.any { it.className.startsWith(APP_PACKAGE) }) {
                        Log.w(TAG, "ThreadPolicy violation", violation)
                    }
                }
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .penaltyListener(executor) { violation ->
                    if (violation.stackTrace.any { it.className.startsWith(APP_PACKAGE) }) {
                        Log.w(TAG, "VmPolicy violation", violation)
                    }
                }
                .build()
        )
    }

    private fun setupStrictModeLegacy() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .penaltyLog()
                .build()
        )
    }

    private fun setupLogging() {
        setupTimberLogging()
    }
}