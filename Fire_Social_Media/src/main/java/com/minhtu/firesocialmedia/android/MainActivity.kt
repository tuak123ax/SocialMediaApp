package com.minhtu.firesocialmedia.android

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.minhtu.firesocialmedia.MainApplication
import com.minhtu.firesocialmedia.services.database.DatabaseHelper
import com.minhtu.firesocialmedia.services.remoteconfig.FetchResultCallback
import com.minhtu.firesocialmedia.services.remoteconfig.RemoteConfigHelper
import com.minhtu.firesocialmedia.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var downloadReceiver: BroadcastReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RemoteConfigHelper.getRemoteConfig()
                    fetchDataFromRemoteConfig(object : FetchResultCallback {
                        override fun fetchSuccess() {
                            Log.e("Fetch", "Fetch success")
                        }

                        override fun fetchFail() {
                            Log.e("Fetch", "Fetch fail")
                        }
                    })
                    MainApplication.MainApp(this)
                    checkFCMToken(applicationContext)
                    askNotificationPermission()
                    //Listen download event here to show toast on all screens
                    listenDownloadImageEvent()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    private fun listenDownloadImageEvent() {
        if(downloadReceiver == null) {
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?
                ) {
                    if(intent != null && intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if(downloadId > -1) {
                            Toast.makeText(this@MainActivity, "Download image successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    downloadReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
        }
    }

    private fun checkFCMToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the new FCM token
                    val token = task.result
                    Log.d("FCM", "FCM Token: $token")
                    Utils.updateTokenInStorage(token, context)
                }
            }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun fetchDataFromRemoteConfig(fetchResultCallback: FetchResultCallback) {
        RemoteConfigHelper.fetchAndActiveConfig(RemoteConfigHelper.getRemoteConfig(), fetchResultCallback)
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun GreetingPreview() {
    }
}
