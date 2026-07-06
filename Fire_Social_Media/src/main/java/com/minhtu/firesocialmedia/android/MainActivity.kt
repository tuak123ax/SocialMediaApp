package com.minhtu.firesocialmedia.android

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessaging
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.di.AndroidPlatformContext
import com.minhtu.firesocialmedia.di.PlatformContextHolder
import com.minhtu.firesocialmedia.android.service.serviceimpl.permission.AndroidPermissionManager
import com.minhtu.firesocialmedia.android.service.serviceimpl.remoteconfig.FetchResultCallback
import com.minhtu.firesocialmedia.android.service.serviceimpl.remoteconfig.RemoteConfigHelper
import com.minhtu.firesocialmedia.platform.MainApplication
import com.minhtu.firesocialmedia.platform.TokenStorage.updateTokenInStorage
import com.minhtu.firesocialmedia.ui.theme.FireSocialMediaCommonTheme

class MainActivity : ComponentActivity() {
    private var downloadReceiver: BroadcastReceiver? = null
    private lateinit var permissionManager: AndroidPermissionManager

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        permissionManager.onPermissionsResult(grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deepLink = intent.data.toString()
        //Check if activity is started from notification
        val fromNotification = intent.getBooleanExtra(Constants.FROM_NOTIFICATION, false)
        permissionManager = AndroidPermissionManager(this)
        permissionManager.setPermissionLauncher(requestMultiplePermissionsLauncher)
        PlatformContextHolder.instance = AndroidPlatformContext(applicationContext, permissionManager)
        setContent {
            FireSocialMediaCommonTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RemoteConfigHelper.getRemoteConfig()
                    var minAppVersion by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(Unit) {
                        fetchDataFromRemoteConfig(object : FetchResultCallback {
                            override fun fetchSuccess(minVersion : String) {
                                Log.e("Fetch", "Fetch success: min version = $minVersion")
                                minAppVersion = minVersion
                            }

                            override fun fetchFail() {
                                Log.e("Fetch", "Fetch fail")
                            }
                        })
                        checkFCMToken()
                        askNotificationPermission()
                    }
                    val platformContext = remember { AndroidPlatformContext(applicationContext, permissionManager) }
                    Box(modifier = Modifier.fillMaxSize()) {
                        if(fromNotification) {
                            val sessionId = intent.getStringExtra("sessionId")
                            val callerId = intent.getStringExtra("callerId")
                            val calleeId = intent.getStringExtra("calleeId")
                            MainApplication.MainAppFromNotification(
                                this@MainActivity,
                                platformContext,
                                sessionId,
                                callerId,
                                calleeId
                            )
                        } else {
                            if(deepLink.isNotEmpty()) {
                                MainApplication.MainAppWithDeepLink(this@MainActivity, deepLink, platformContext)
                            } else {
                                MainApplication.MainApp(this@MainActivity, platformContext)
                            }
                        }
                        // Dialog is drawn last inside Box, so it appears on top
                        minAppVersion?.let { CheckAppVersionAndShowDialog(it) }
                    }
                    //Listen download event here to show toast on all screens
//                    listenDownloadImageEvent()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            downloadReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {
        } finally {
            downloadReceiver = null
        }
        runCatching { permissionManager.clear() }
    }

    private fun checkFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the new FCM token
                    val token = task.result
                    Log.d("FCM", "FCM Token: $token")
                    updateTokenInStorage(token)
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
        RemoteConfigHelper.fetchMinAppSupportAndActiveConfig(RemoteConfigHelper.getRemoteConfig(), fetchResultCallback)
    }


    @Composable
    fun CheckAppVersionAndShowDialog(minSupportVersion: String) {
        val currentVersion = BuildConfig.VERSION_NAME
        if (isVersionLower(currentVersion, minSupportVersion)) {
            // Show update dialog
            UpdateAppDialog()
        }
    }

    fun isVersionLower(currentVersion: String, minSupportVersion: String): Boolean {
        val currentParts = currentVersion.split(".")
        val minParts = minSupportVersion.split(".")
        val maxLength = maxOf(currentParts.size, minParts.size)

        for (i in 0 until maxLength) {
            val currentPart = if (i < currentParts.size) currentParts[i].toIntOrNull() ?: 0 else 0
            val minPart = if (i < minParts.size) minParts[i].toIntOrNull() ?: 0 else 0

            if (currentPart < minPart) {
                return true
            } else if (currentPart > minPart) {
                return false
            }
        }
        return false // Versions are equal
    }

    @Composable
    fun UpdateAppDialog() {

        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = "Update Required")
            },
            text = {
                Text(text = "Please update the app to continue using it.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = "https://github.com/tuak123ax/SocialMediaApp".toUri()
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Update Now")
                }
            }
        )
    }
}
