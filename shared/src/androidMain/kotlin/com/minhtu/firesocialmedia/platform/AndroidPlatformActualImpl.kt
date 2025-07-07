package com.minhtu.firesocialmedia.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.services.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.services.notification.Client
import com.minhtu.firesocialmedia.services.notification.NotificationApiService
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.russhwolf.settings.Settings
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import com.seiko.imageloader.option.androidContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
        "more_horiz" -> painterResource(id = R.drawable.more_horiz)
        "arrow_back" -> painterResource(id = R.drawable.arrow_back)
        else -> null
    }
}

@Composable
actual fun getIconComposable(icon: String, color : String, tint : String?, modifier : Modifier): (@Composable () -> Unit)? = null

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
        color = "#00FFFFFF",
        contentDescription = descriptionOfIcon,
        modifier = Modifier.size(20.dp)
    )
}

actual fun exitApp() {
    (appContext as Activity).finish()
}

actual fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance): String {
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

        body.put(Constants.REMOTE_MSG_DATA, data)
        body.put(Constants.REMOTE_MSG_TOKENS, tokens)
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

actual fun logMessage(tag: String, message :String) {
    Log.e(tag, message)
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

class AndroidImagePicker(
    private val onImagePicked: (String) -> Unit,
    private val onVideoPicked: (String) -> Unit
) : ImagePicker {
    enum class PickType { IMAGE, VIDEO }
    var currentPickType: PickType? = null
    private lateinit var launcher: ActivityResultLauncher<Intent>
    @Composable
    override fun RegisterLauncher(hideLoading : () -> Unit) {
        launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                Log.e("getAvatarFromGalleryLauncher", "RESULT_OK")
                val dataUrl = result.data?.data
                if(dataUrl != null){
                    Log.e("getAvatarFromGalleryLauncher", dataUrl.toString())
                    if(currentPickType == PickType.IMAGE){
                        onImagePicked(dataUrl.toString())
                    } else {
                        onVideoPicked(dataUrl.toString())
                    }
                    hideLoading()
                }
            } else {
                if(result.resultCode == Activity.RESULT_CANCELED)
                {
                    currentPickType = null
                    hideLoading()
                }
            }
        }
    }

    override fun pickImage() {
        currentPickType = PickType.IMAGE
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        launcher.launch(intent)
    }

    override fun pickVideo() {
        currentPickType = PickType.VIDEO
        val intent = Intent()
        intent.setType("video/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        launcher.launch(intent)
    }

    override suspend fun loadImageBytes(uri: String): ByteArray? {
         return try {
            val parsedUri = uri.toUri()
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(appContext.contentResolver, parsedUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(appContext.contentResolver, parsedUri)
            }

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    override fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier) {
        if(byteArray != null) {
            val bitmap = remember(byteArray) { byteArray.toBitmap() }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            }
        }
    }

    fun ByteArray.toBitmap(): Bitmap? {
        return BitmapFactory.decodeByteArray(this, 0, size)
    }
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

    override fun navigateBack() {
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
    onStopCall : () -> Unit,
    modifier: Modifier
) {
    val eglBase = remember { EglBase.create() }

    Box(modifier = Modifier.fillMaxSize()) {
        remoteTrack?.track?.let {
            RemoteVideoView(
                eglBaseContext = eglBase.eglBaseContext,
                videoTrack = it,
                modifier = Modifier.fillMaxSize()
            )
        }

        localTrack?.track?.let {
            LocalVideoView(
                eglBaseContext = eglBase.eglBaseContext,
                videoTrack = it,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        var backgroundButton by remember {mutableStateOf(Color.Red)}
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape) // Shadow before clipping
                .clip(CircleShape)
                .background(backgroundButton)
                .testTag(TestTag.TAG_REJECT_CALL_BUTTON)
                .semantics {
                    contentDescription = TestTag.TAG_REJECT_CALL_BUTTON
                }
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    backgroundButton = Color.Gray
                    onStopCall()
                },
                shape = CircleShape,
                containerColor = Color.Transparent, // Transparent to let gradient show
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Stop Call", tint = Color.Black)
            }
        }
    }
}

@Composable
fun LocalVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            SurfaceViewRenderer(it).apply {
                init(eglBaseContext, null)
                setMirror(true)
                videoTrack.addSink(this)
            }
        },
        modifier = modifier
    )
}

@Composable
fun RemoteVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            SurfaceViewRenderer(it).apply {
                init(eglBaseContext, null)
                setMirror(false)
                videoTrack.addSink(this)
            }
        },
        modifier = modifier
    )
}

class AndroidPermissionManager(private val activity: Activity) : PermissionManager {

    private var continuation: CancellableContinuation<Boolean>? = null

    override suspend fun requestCameraAndAudioPermissions(): Boolean {
        return requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    override suspend fun requestAudioPermission(): Boolean {
        return requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO))
    }

    private suspend fun requestPermissions(permissions: Array<String>): Boolean {
        return suspendCancellableCoroutine { cont ->
            continuation = cont
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE) return
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        continuation?.resume(granted, onCancellation = {})
        continuation = null
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}

