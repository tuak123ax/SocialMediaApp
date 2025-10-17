package com.minhtu.firesocialmedia.platform

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.data.remote.service.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.call.WebRTCManager
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.imagepicker.AndroidImagePicker
import com.minhtu.firesocialmedia.domain.serviceimpl.notification.Client
import com.minhtu.firesocialmedia.domain.serviceimpl.notification.NotificationApiService
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.russhwolf.settings.BuildConfig
import com.russhwolf.settings.Settings
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import com.seiko.imageloader.option.androidContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.Path.Companion.toOkioPath
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import androidx.activity.compose.BackHandler as AndroidBackHandler

private lateinit var appContext: Context
fun initPlatformContext(context: Context) {
    appContext = context.applicationContext
}
actual fun showToast(message: String) {
    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
}

@Composable
actual fun getIconPainter(icon : String): Painter? {
    return when(icon) {
        "close" -> painterResource(id = R.drawable.close)
        "send_message" -> painterResource(id = R.drawable.send_message)
        "search" -> painterResource(id = R.drawable.search)
        "logout" -> painterResource(id = R.drawable.logout)
        "fire_chat_icon" -> painterResource(id = R.drawable.fire_chat_icon)
        "like" -> painterResource(id = R.drawable.like)
        "comment" -> painterResource(id = R.drawable.comment)
        "white_close" -> painterResource(id = R.drawable.white_close)
        "background" -> painterResource(id = R.drawable.background)
        "visibility" -> painterResource(id = R.drawable.visibility)
        "visibility_off" -> painterResource(id = R.drawable.visibility_off)
        "google" -> painterResource(id = R.drawable.google)
        "more_horiz" -> painterResource(id = R.drawable.more_horiz_24)
        "arrow_back" -> painterResource(id = R.drawable.arrow_back)
        else -> null
    }
}

@Composable
actual fun getIconComposable(icon: String, bgColor : String, tint : String?, modifier : Modifier): (@Composable () -> Unit)? = null

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled, onBack)
}

@Composable
actual fun PasswordVisibilityIcon(passwordVisibility : Boolean) {
    val icon = if(passwordVisibility) "visibility" else "visibility_off"
    val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
    CrossPlatformIcon(
        icon = icon,
        backgroundColor = "#00FFFFFF",
        contentDescription = descriptionOfIcon,
        modifier = Modifier.size(20.dp)
    )
}

actual fun exitApp() {
    (appContext as Activity).finish()
}

actual fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance, type : String): String {
    val body = JSONObject()
    try {
        val tokens = JSONArray()
        for(token in tokenList) {
            tokens.put(token)
        }
        val data = JSONObject()
        data.put(Constants.KEY_FCM_TOKEN, sender.token)
        data.put(Constants.KEY_USER_ID, sender.uid)
        data.put(Constants.KEY_AVATAR, sender.image)
        data.put(Constants.KEY_EMAIL, sender.email)
        data.put(Constants.REMOTE_MSG_TITLE, sender.name)
        data.put(Constants.REMOTE_MSG_BODY, message)
        data.put(Constants.REMOTE_MSG_TYPE, type)

        body.put(Constants.REMOTE_MSG_DATA, data)
        body.put(Constants.REMOTE_MSG_TOKENS, tokens)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return body.toString()
}

actual fun createCallMessage(message: String, tokenList : ArrayList<String>, sessionId : String, sender : UserInstance, receiver : UserInstance, type : String): String {
    val body = JSONObject()
    try {
        val tokens = JSONArray()
        for(token in tokenList) {
            tokens.put(token)
        }
        val data = JSONObject()
        data.put(Constants.KEY_SESSION_ID, sessionId)
        data.put(Constants.KEY_CALLER_ID, sender.uid)
        data.put(Constants.KEY_CALLER_NAME, sender.name)
        data.put(Constants.KEY_CALLER_AVATAR, sender.image)
        data.put(Constants.KEY_CALLEE_ID, receiver.uid)
        data.put(Constants.KEY_CALLEE_NAME, receiver.name)
        data.put(Constants.KEY_CALLEE_AVATAR, receiver.image)
        data.put(Constants.REMOTE_MSG_BODY, message)
        data.put(Constants.REMOTE_MSG_TYPE, type)

        body.put(Constants.REMOTE_MSG_DATA, data)
        body.put(Constants.REMOTE_MSG_TOKENS, tokens)
        body.put(Constants.KEY_FCM_PRIORITY, "high")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return body.toString()
}

actual fun sendMessageToServer(request: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = Client.getClient(Constants.APP_SCRIPT_URL)?.create(NotificationApiService::class.java)!!
                .sendToAppScript(request).execute()
            if (response.isSuccessful) {
//                Log.d("FCM", "Notification Sent Successfully: ${response.body()}")
            } else {
//                Log.e("FCM", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
//            Log.e("FCM", "Exception: ${e.message}")
        }
    }
}

actual object TokenStorage {
    actual fun updateTokenInStorage(token: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(appContext)
            secureSharedPreferences.edit() { putString(Constants.KEY_FCM_TOKEN, token) }
        }
    }
}

actual inline fun logMessage(tag: String, message: () -> String) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message())
    }
}

actual fun generateRandomId(): String {
    return UUID.randomUUID().toString()
}

actual fun getCurrentTime() : Long{
    //Get current time in milliseconds
    return System.currentTimeMillis()
}

actual fun convertTimeToDateString(time : Long) : String{
    //Convert time in milliseconds to date string
    return SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(time))
}

actual fun getRandomIdForNotification() : String {
    return "Noti-" + UUID.randomUUID().toString()
}

actual suspend fun getImageBytesFromDrawable(name: String): ByteArray?{
    val resId = appContext.resources.getIdentifier(name, "drawable", appContext.packageName)
    val drawable = appContext.getDrawable(resId) ?: return null

    val bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

actual fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        options {
            androidContext(appContext)
        }
        components {
            setupDefaultComponents()
        }
        interceptor {
            // cache 25% memory bitmap
            bitmapMemoryCacheConfig {
                maxSizePercent(appContext, 0.25)
            }
            // cache 50 image
            imageMemoryCacheConfig {
                maxSize(50)
            }
            // cache 50 painter
            painterMemoryCacheConfig {
                maxSize(50)
            }
            diskCacheConfig {
                directory(appContext.cacheDir.resolve("image_cache").toOkioPath())
                maxSizeBytes(512L * 1024 * 1024) // 512MB
            }
        }
    }
}

class AndroidNavigationHandler(
    private val navController: NavController
) : NavigationHandler {
    // Expose the current route as a mutable state
    private var _currentRoute = mutableStateOf<String?>(null)

    @Composable
    fun ObserveCurrentRoute() {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        _currentRoute.value = navBackStackEntry.value?.destination?.route
    }
    override fun navigateTo(route: String) {
        navController.navigate(route)
    }

    override fun navigateBack(){
        navController.popBackStack()
    }

    override fun getCurrentRoute() : String? {
        return _currentRoute.value
    }
}

actual fun onPushNotificationReceived(data: Map<String, Any?>) {
}

actual val settings: Settings? = null

@Composable
actual fun VideoPlayer(uri: String, modifier: Modifier) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = false
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                this.player = player
            }
        }
    )
}


actual class WebRTCVideoTrack(val track: VideoTrack?)

@Composable
actual fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    modifier: Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        remoteTrack?.track?.let {
            RemoteVideoView(
                eglBaseContext = WebRTCManager.eglBase.eglBaseContext,
                videoTrack = it,
                modifier = Modifier.fillMaxSize()
            )
        }

        localTrack?.track?.let {
            LocalVideoView(
                eglBaseContext = WebRTCManager.eglBase.eglBaseContext,
                videoTrack = it,
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun LocalVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val renderer = remember {
        SurfaceViewRenderer(context).apply {
            init(eglBaseContext, null)
            setZOrderMediaOverlay(true)
            setMirror(true)
            setEnableHardwareScaler(true)
        }
    }

    DisposableEffect(videoTrack) {
        videoTrack.addSink(renderer)

        onDispose {
            runCatching {
                videoTrack.removeSink(renderer)
            }.onFailure {
                Log.e("LocalVideoView", "Failed to remove sink", it)
            }

            runCatching {
                renderer.release()
            }.onFailure {
                Log.e("LocalVideoView", "Failed to release renderer", it)
            }
        }
    }

    AndroidView(factory = { renderer }, modifier = modifier)
}


@Composable
fun RemoteVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val renderer = remember {
        SurfaceViewRenderer(context).apply {
            init(eglBaseContext, null)
            setMirror(false)
            setEnableHardwareScaler(true)
        }
    }

    DisposableEffect(videoTrack) {
        videoTrack.addSink(renderer)

        onDispose {
            runCatching {
                videoTrack.removeSink(renderer)
            }.onFailure {
                Log.e("RemoteVideoView", "Failed to remove sink", it)
            }

            runCatching {
                renderer.release()
            }.onFailure {
                Log.e("RemoteVideoView", "Failed to release renderer", it)
            }
        }
    }

    AndroidView(factory = { renderer }, modifier = modifier)
}

@Composable
actual fun rememberNavigationHandler(navController: Any): NavigationHandler {
    val controller = navController as NavController
    val handler = remember(controller) { AndroidNavigationHandler(controller) }
    // Keep handler's current route in sync with NavController
    handler.ObserveCurrentRoute()
    return handler
}

@Composable
actual fun <T : Any> platformViewModel(key: String?, factory: () -> T): T {
    // For KMP ViewModels (not AndroidX ViewModel), just remember per key/type
    return remember(key) { factory() }
}


@Composable
actual fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit,
    onVideoPicked: (String) -> Unit
): ImagePicker {
    val ctx = when (context) {
        is Activity -> context
        is Context -> context
        else -> LocalContext.current
    }
    return remember(ctx) { AndroidImagePicker(ctx, onImagePicked, onVideoPicked) }
}

@Composable
actual fun setupSignInLauncher(
    context: Any?,
    signInViewModel: SignInViewModel,
    platformContext: PlatformContext
) {
    val activity = when (context) {
        is Activity -> context
        is Context -> context
        else -> LocalContext.current
    }
    val signInGoogleResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            try {
                val task = Identity.getSignInClient(activity).getSignInCredentialFromIntent(result.data)
                signInViewModel.handleSignInResult(task)
            } catch (e: Exception) {
                logMessage("SignIn") { "Exception: ${e.message}" }
                signInViewModel.updateSignInStatus(SignInState(false, null))
            }
        }
    )
    LaunchedEffect(Unit) {
        signInViewModel.setSignInLauncher(object : SignInLauncher {
            override fun launchGoogleSignIn() {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId("744458948813-qktjfopd2cr9b1a87pbr3981ujllb3mt.apps.googleusercontent.com")
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()
                val googleSignInClient = Identity.getSignInClient(activity)
                googleSignInClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                        signInGoogleResultLauncher.launch(intentSenderRequest)
                    } catch (e: android.content.IntentSender.SendIntentException) {
                        logMessage("OneTapSignIn") { "Error launching intent: ${e.localizedMessage}" }
                    }
                }.addOnFailureListener { exception ->
                    logMessage("OneTapSignIn") { "Sign-in failed: ${exception.localizedMessage}" }
                }
            }
        })
    }
}

