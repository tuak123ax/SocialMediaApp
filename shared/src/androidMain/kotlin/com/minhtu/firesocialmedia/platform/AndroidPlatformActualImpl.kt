package com.minhtu.firesocialmedia.platform

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import android.net.Uri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.minhtu.firesocialmedia.BuildConfig
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.data.remote.service.signinlauncher.SignInLauncher
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.data.remote.mapper.authentication.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.authentication.toDTO
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.home.deeplinks.ShareApp
import com.minhtu.firesocialmedia.core.domain.entity.signin.SignInState
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.auth.AuthenticationApiService
import com.minhtu.firesocialmedia.domain.serviceimpl.call.WebRTCManager
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.imagepicker.AndroidImagePicker
import com.minhtu.firesocialmedia.domain.serviceimpl.notification.Client
import com.minhtu.firesocialmedia.domain.serviceimpl.notification.NotificationApiService
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.toast.ToastController
import com.minhtu.firesocialmedia.utils.NavigationHandler
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
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import qrcode.QRCode
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import kotlin.system.exitProcess
import androidx.activity.compose.BackHandler as AndroidBackHandler

private lateinit var appContext: Context
fun initPlatformContext(context: Context) {
    appContext = context
}
internal fun getAppContext(): Context = appContext

@SuppressLint("UnsafeOptInUsageError")
@Volatile
private var videoSimpleCache: SimpleCache? = null

// Lock object for synchronization
private val videoCacheLock = Any()

@OptIn(UnstableApi::class)
private fun getVideoCache(context: Context): SimpleCache {
    // First fast path (no lock)
    videoSimpleCache?.let { return it }

    synchronized(videoCacheLock) {
        // Second check inside lock
        videoSimpleCache?.let { return it }

        val cacheDir = File(context.cacheDir, "video_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(512L * 1024L * 1024L) // 512MB
        val created = SimpleCache(cacheDir, evictor)

        videoSimpleCache = created
        return created
    }
}
actual fun showToast(message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        ToastController.show(message)
    }
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
        "draft" -> painterResource(id = R.drawable.draft)
        "nothing_here" -> painterResource(id = R.drawable.nothing_here)
        "share" -> painterResource(id = R.drawable.share)
        "public" -> painterResource(id = R.drawable.public_access)
        "private" -> painterResource(id = R.drawable.private_access)
        "onlyFriends" -> painterResource(id = R.drawable.only_friends)
        "down_arrow" -> painterResource(id = R.drawable.down_arrow)
        "group" -> painterResource(id = R.drawable.group)
        "group_background" -> painterResource(id = R.drawable.group_background)
        "create_group" -> painterResource(id = R.drawable.create_group)
        "explore_group" -> painterResource(id = R.drawable.explore_group)
        "right" -> painterResource(id = R.drawable.right)
        "select_group" -> painterResource(id = R.drawable.select_group)
        "right_arrow" -> painterResource(id = R.drawable.right_arrow)
        "global" -> painterResource(id = R.drawable.global)
        "add_member" -> painterResource(id = R.drawable.add_member)
        "image" -> painterResource(id = R.drawable.image)
        "mute" -> painterResource(id = R.drawable.mute)
        "unmute" -> painterResource(id = R.drawable.unmute)
        "speaker" -> painterResource(id = R.drawable.speaker)
        "no_sound" -> painterResource(id = R.drawable.no_sound)
        "audio" -> painterResource(id = R.drawable.audio)
        "video_call" -> painterResource(id = R.drawable.video_call)
        "toast" -> painterResource(id = R.drawable.toast_icon)
        else -> null
    }
}

actual fun getResId(icon : String): Int {
    return when(icon) {
        "loading_gif" -> R.raw.loading_gif
        "dialga" -> R.raw.dialga
        else -> {0}
    }
}

@Composable
actual fun getIconComposable(icon: String, bgColor : String, tint : String?, modifier : Modifier): (@Composable () -> Unit)? = null

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled, onBack)
}

actual fun exitApp() {
    // Finish and remove app task
    val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appTaskList = am.appTasks

    if (!appTaskList.isNullOrEmpty()) {
        val appTask = appTaskList[0]
        appTask.finishAndRemoveTask()
    }

    // Kill process and exit
    try {
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    } catch (_: Exception) {
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }
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
                logMessage("sendMessageToFCM",
                    { "Notification Sent Successfully: ${response.body()}" })
            } else {
                logMessage("sendMessageToFCM",
                    { "Error: \${response.errorBody()?.string()}" })
            }
        } catch (e: Exception) {
            logMessage("sendMessageToFCM", { "Error: ${e.message}" })
        }
    }
}
actual suspend fun send2FARequest(request: TwoFARequest): TwoFAResponse {
    val response = Client.getClient(Constants.APP_SCRIPT_URL)
        ?.create(AuthenticationApiService::class.java)!!
        .sendVerifyRequestToAppScript(request.toDTO())
        .execute()

    return if (response.isSuccessful) {
        val twoFAResponseDTO = response.body()
        logMessage("sendVerifyOTPRequest") {
            "Success: ${response.code()} | success=${twoFAResponseDTO?.success} | message=${twoFAResponseDTO?.message}"
        }
        twoFAResponseDTO?.toDomain() ?: TwoFAResponse(false, "Error happened. Please try again!")
    } else {
        val errorBody = response.errorBody()?.string()
        logMessage("sendVerifyOTPRequest") {
            """
            Request failed:
            - Code: ${response.code()}
            - Message: ${response.message()}
            - Error Body: $errorBody
            """.trimIndent()
        }
        TwoFAResponse(false, response.code().toString())
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
        // Use println to avoid android.util.Log which isn't available in unit tests
        println("$tag: ${message()}")
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

actual suspend fun getImageBytesFromDrawable(name: String): ByteArray? = withContext(Dispatchers.IO) {
    val resId = appContext.resources.getIdentifier(name, "drawable", appContext.packageName)
    val drawable = appContext.getDrawable(resId) ?: return@withContext null

    val bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.toByteArray()
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
actual fun VideoPlayer(
    uri: String,
    modifier: Modifier,
    isLiked : Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)?
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    // Toggle to force the inline PlayerView to re-bind the player after exiting fullscreen
    var playerBindVersion by remember { mutableStateOf(0) }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = false
    }

    PlayerViewContent(
        player = player,
        playerBindVersion = playerBindVersion,
        modifier = modifier,
        onFullscreenClick = {
            isFullscreen = true
        }
    )

    if (isFullscreen) {
        FullscreenVideoDialog(
            player = player,
            activity = activity,
            isLiked = isLiked,
            onDismiss = {
                isFullscreen = false
                // Increment version so PlayerViewContent's update block re-binds the player
                playerBindVersion++
            },
            onLikeClick = { onLikeClick() },
            onCommentClick = { onCommentClick() },
            onShareClick = { onShareClick() },
            commentSheetContent = commentSheetContent
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerViewContent(
    player: ExoPlayer,
    playerBindVersion: Int,
    modifier: Modifier,
    onFullscreenClick: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setFullscreenButtonClickListener {
                    onFullscreenClick()
                }
            }
        },
        update = { view ->
            // playerBindVersion change forces this block to run, re-attaching player
            // to the inline surface after exiting fullscreen
            @Suppress("UNUSED_EXPRESSION") playerBindVersion
            view.player = null
            view.player = player
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun FullscreenVideoDialog(
    player: ExoPlayer,
    activity: Activity?,
    isLiked : Boolean,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)? = null
) {
    BackHandler {
        onDismiss()
    }

    // Hold a reference to the like button so we can update it imperatively when isLiked changes
    val likeButtonRef = remember { mutableStateOf<LinearLayout?>(null) }
    // Hold reference to the playerView so we can resize it when comment sheet opens/closes
    val playerViewRef = remember { mutableStateOf<PlayerView?>(null) }

    // Reactively update the like button's visual state whenever isLiked flips
    LaunchedEffect(isLiked) {
        val btn = likeButtonRef.value
        val ctx = activity ?: return@LaunchedEffect
        if (btn != null) updateLikeButtonState(btn, isLiked, ctx)
    }

    // Comment sheet state
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // When sheet opens/closes, resize the player view to fit above the sheet
    LaunchedEffect(showCommentSheet) {
        val pv = playerViewRef.value ?: return@LaunchedEffect
        val window = activity?.window ?: return@LaunchedEffect
        if (showCommentSheet) {
            // Shrink player to top 45% of screen
            val screenHeight = window.decorView.height
            val targetHeight = (screenHeight * 0.45f).toInt()
            pv.layoutParams = (pv.layoutParams as FrameLayout.LayoutParams).apply {
                height = targetHeight
                gravity = Gravity.TOP
            }
        } else {
            // Restore to full screen
            pv.layoutParams = (pv.layoutParams as FrameLayout.LayoutParams).apply {
                height = FrameLayout.LayoutParams.MATCH_PARENT
                gravity = Gravity.NO_GRAVITY
            }
        }
        pv.requestLayout()
    }

    DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}

        // Edge-to-edge fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // ── Root container ──────────────────────────────────────────────────
        val container = FrameLayout(activity).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // ── Player ──────────────────────────────────────────────────────────
        val playerView = PlayerView(activity).apply {
            this.player = player
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            fitsSystemWindows = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setFullscreenButtonClickListener { onDismiss() }
        }
        playerViewRef.value = playerView

        // ── Bottom gradient scrim (makes controller text readable) ──────────
        val bottomScrim = android.view.View(activity).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(
                    android.graphics.Color.argb(180, 0, 0, 0),
                    android.graphics.Color.TRANSPARENT
                )
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                160.dpToPx(activity),
                Gravity.BOTTOM
            )
        }

        // ── Top gradient scrim ───────────────────────────────────────────────
        val topScrim = android.view.View(activity).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    android.graphics.Color.argb(120, 0, 0, 0),
                    android.graphics.Color.TRANSPARENT
                )
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                100.dpToPx(activity),
                Gravity.TOP
            )
        }

        // ── Action column (TikTok-style right side) ──────────────────────────
        val likeButton = modernActionButton(activity, R.drawable.like, "Like", onLikeClick, isLiked)
        likeButtonRef.value = likeButton

        // Comment button opens the in-place sheet instead of navigating away
        val commentClickAction: () -> Unit = if (commentSheetContent != null) {
            { showCommentSheet = true }
        } else {
            onCommentClick
        }

        val actionColumn = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 16.dpToPx(activity), 110.dpToPx(activity))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.BOTTOM
            )

            addView(likeButton)
            addView(modernActionButton(activity, R.drawable.comment, "Comment", commentClickAction))
            addView(modernActionButton(activity, R.drawable.share, "Share", onShareClick))
        }

        // ── Sync visibility + fade with player controller ────────────────────
        playerView.setControllerVisibilityListener(
            PlayerView.ControllerVisibilityListener { visibility ->
                val targetAlpha = if (visibility == android.view.View.VISIBLE) 1f else 0f
                listOf(actionColumn, bottomScrim, topScrim).forEach { v ->
                    ObjectAnimator.ofFloat(v, "alpha", v.alpha, targetAlpha).apply {
                        duration = 250
                        interpolator = AccelerateDecelerateInterpolator()
                        start()
                    }
                }
            }
        )

        // ── Compose layers ───────────────────────────────────────────────────
        container.addView(playerView)
        container.addView(bottomScrim)
        container.addView(topScrim)
        container.addView(actionColumn)

        val decorView = window.decorView as ViewGroup
        decorView.addView(container)

        onDispose {
            likeButtonRef.value = null
            playerViewRef.value = null
            val wasPlaying = player.isPlaying
            val position = player.currentPosition
            playerView.player = null
            decorView.removeView(container)
            WindowCompat.setDecorFitsSystemWindows(window, true)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            player.seekTo(position)
            if (wasPlaying) player.play()
        }
    }

    // ── Comment bottom sheet (Compose layer, shown above the video) ──────────
    if (showCommentSheet && commentSheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Use fillMaxHeight(0.5f) so the sheet content (list + input) is fully visible
            // in the half-expanded state without requiring the user to scroll the sheet up.
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
                commentSheetContent { showCommentSheet = false }
                // Toast host must be placed here so toasts appear above the fullscreen
                // video overlay (which is added directly to the decor view and sits
                // behind the normal Compose ToastHost).
                ToastController.ToastHost()
            }
        }
    }
}


actual class WebRTCVideoTrack(val track: VideoTrack?)

fun isUsableVideoTrack(track: VideoTrack?): Boolean {
    if (track == null) return false

    return try {
        track.enabled()
    } catch (e: Exception) {
        false
    }
}

@Composable
actual fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    isLocalVideoOff : Boolean,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        // Keep a black remote canvas when remote video is absent.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        remoteTrack?.track?.takeIf { isUsableVideoTrack(it) }?.let { track ->
            RemoteVideoView(
                eglBaseContext = WebRTCManager.eglBase.eglBaseContext,
                videoTrack = track,
                modifier = Modifier.fillMaxSize()
            )
        }

        if(!isLocalVideoOff) {
            localTrack?.track?.takeIf { isUsableVideoTrack(it) }?.let {
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
}

@Composable
fun LocalVideoView(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    var currentRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    DisposableEffect(videoTrack, currentRenderer) {
        val renderer = currentRenderer
        if (renderer != null && isUsableVideoTrack(videoTrack)) {
            // Defensive rebind for renegotiation/rejoin: ensure stale bindings are detached first.
            runCatching { videoTrack.removeSink(renderer) }
            runCatching { videoTrack.addSink(renderer) }
                .onFailure { Log.w("LocalVideoView", "Skip addSink on disposed local track", it) }
        }
        onDispose {
            if (renderer != null && isUsableVideoTrack(videoTrack)) {
                runCatching { videoTrack.removeSink(renderer) }
                    .onFailure { Log.e("LocalVideoView", "Failed to remove sink", it) }
                renderer.clearImage()
            }
        }
    }

    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglBaseContext, null)
                setZOrderMediaOverlay(true)
                setMirror(true)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                // Avoid SurfaceFlinger buffer-size rejection on dynamic Compose layouts.
                setEnableHardwareScaler(false)
                currentRenderer = this
            }
        },
        onRelease = { renderer ->
            currentRenderer = null
            if (isUsableVideoTrack(videoTrack)) {
                runCatching {
                    try {
                        videoTrack.removeSink(renderer)
                    } catch (_: Exception) {}
                }
            }
            renderer.clearImage()
            renderer.release()
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
    var currentRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    DisposableEffect(videoTrack, currentRenderer) {
        val renderer = currentRenderer
        if (renderer != null) {
            // Defensive rebind: if this renderer was previously attached to another
            // wrapper, detach first so renegotiation can rebind cleanly.
            runCatching { videoTrack.removeSink(renderer) }
            runCatching { videoTrack.addSink(renderer) }
                .onFailure { Log.w("RemoteVideoView", "Failed to add remote sink", it) }
        }
        onDispose {
            if (renderer != null) {
                runCatching { videoTrack.removeSink(renderer) }
                    .onFailure { Log.e("RemoteVideoView", "Failed to remove sink", it) }
                renderer.clearImage()
            }
        }
    }

    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglBaseContext, null)
                setMirror(false)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                // Avoid SurfaceFlinger buffer-size rejection on dynamic Compose layouts.
                setEnableHardwareScaler(false)
                currentRenderer = this
            }
        },
        onRelease = { renderer ->
            currentRenderer = null
            runCatching {
                try {
                    videoTrack.removeSink(renderer)
                } catch (_: Exception) {}
            }
            renderer.clearImage()
            renderer.release()
        },
        modifier = modifier
    )
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

actual fun getUriStringFromLocalPath(localPath : String) : String {
    return Uri.fromFile(File(localPath)).toString()
}

actual suspend fun queryShareApps(text: String): MutableList<ShareApp> {
    val pm = appContext.packageManager

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    val resolveInfos = pm.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfos.map { ri ->
        ShareApp(
            name = ri.loadLabel(pm).toString(),
            packageName = ri.activityInfo.packageName,
            activityName = ri.activityInfo.name,
            icon = ri.loadIcon(pm).toBitmap()
        )
    }.toMutableList()
}

actual fun launchShareAppWithDeepLink(app : ShareApp, deepLink : String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, deepLink)
        `package` = app.packageName
        setClassName(app.packageName, app.activityName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    appContext.startActivity(intent)
}

actual fun getAppVersion(): String {
    val pm = appContext.packageManager
    val pkg = appContext.packageName

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageInfo(
            pkg,
            PackageManager.PackageInfoFlags.of(0)
        ).versionName
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(pkg, 0).versionName
    } ?: ""
}

actual fun generateQrImage(content: String): ImageBitmap {
    val bytes = QRCode.ofSquares().build(content).renderToBytes()
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap.asImageBitmap()
}

actual object AppConfig {
    actual val twoFAApiKey: String = BuildConfig.APP_SCRIPT_FOR_2FA_AUTHENTICATION_API_KEY
    actual val supabaseApiKey: String = BuildConfig.SUPABASE_API_KEY
    actual val ipInfoApiKey: String = BuildConfig.IPINFO_API_KEY
}
