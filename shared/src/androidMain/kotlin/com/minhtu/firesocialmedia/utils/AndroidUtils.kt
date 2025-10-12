package com.minhtu.firesocialmedia.utils

import android.content.Context
import android.content.Intent
import android.os.Build

class AndroidUtils {
    companion object{
        fun startForegroundService(context : Context, intent : Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}