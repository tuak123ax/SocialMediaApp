package com.minhtu.firesocialmedia.platform

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.minhtu.firesocialmedia.core.BuildConfig
import com.minhtu.firesocialmedia.core.R
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.AndroidCryptoHelper
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
import qrcode.QRCode
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import kotlin.system.exitProcess
import androidx.activity.compose.BackHandler as AndroidBackHandler

private const val KEY_FCM_TOKEN = "fcm_token"

private lateinit var appContext: Context
fun initPlatformContext(context: Context) {
    appContext = context
}
fun getAppContext(): Context = appContext

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


actual object TokenStorage {
    actual fun updateTokenInStorage(token: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(appContext)
            secureSharedPreferences.edit() { putString(KEY_FCM_TOKEN, token) }
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


actual fun getUriStringFromLocalPath(localPath : String) : String {
    return Uri.fromFile(File(localPath)).toString()
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
