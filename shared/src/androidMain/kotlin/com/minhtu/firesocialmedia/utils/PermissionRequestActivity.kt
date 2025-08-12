package com.minhtu.firesocialmedia.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.minhtu.firesocialmedia.data.model.call.CallAction
import com.minhtu.firesocialmedia.domain.serviceimpl.call.CallActionBroadcastReceiver
import com.minhtu.firesocialmedia.platform.showToast

class PermissionRequestActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val grantedIntent = Intent(applicationContext, CallActionBroadcastReceiver::class.java).apply {
            action = CallAction.REQUEST_PERMISSION_ACTION
        }
        if (granted) {
            grantedIntent.putExtra("granted", true)
            showToast("Permission is granted. Please accept call again!")
        } else {
            grantedIntent.putExtra("granted", false)
            showToast("Permission is denied. You cannot use audio call feature!")
        }
        sendBroadcast(grantedIntent)
        finish() // Always close the activity after
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            finish()
        }
    }
}