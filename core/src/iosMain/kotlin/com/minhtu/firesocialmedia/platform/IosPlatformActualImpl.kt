package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.getBytes
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerMediaType
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val REMOTE_MSG_TOKENS = "tokens"
private const val REMOTE_MSG_TYPE = "type"
private const val REMOTE_MSG_DATA = "data"
private const val REMOTE_MSG_TITLE = "title"
private const val REMOTE_MSG_BODY = "body"
private const val KEY_FCM_TOKEN = "fcm_token"
private const val KEY_USER_ID = "user_id"
private const val KEY_AVATAR = "avatar"
private const val KEY_EMAIL = "email"
private const val APP_SCRIPT_URL = "https://script.google.com/macros/s/"
private const val APP_SCRIPT_ENDPOINT = "AKfycbw4JXnBNCl-hoHi2l0_l-Ugp-9icTBWPJVR5PyKqe5o7-JJ-p26yFVpBO8kUZhxtUSzWA/exec"

object ToastController {
    val toastMessage = mutableStateOf<String?>(null)

    fun show(message: String) {
        toastMessage.value = message
    }

    fun dismiss() {
        toastMessage.value = null
    }
}

@Composable
fun ToastHost() {
    val message = ToastController.toastMessage.value

    if (message != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(text = message, color = Color.White)
            }
        }

        LaunchedEffect(message) {
            delay(2000)
            ToastController.dismiss()
        }
    }
}
actual fun showToast(message: String) {
    ToastController.show(message)
}


@Composable
actual fun getIconPainter(icon: String): Painter? {
    return null // iOS will use the composable fallback instead
}

actual fun getResId(icon: String): Int {
    // iOS does not use integer resource IDs like Android; return a sentinel value.
    return 0
}

@Composable
actual fun getIconComposable(icon: String,
                             bgColor : String,
                             tint : String?,
                             modifier : Modifier): (@Composable () -> Unit)? {
    val uiImage = UIImage.imageNamed(icon)
    return {
        if(uiImage != null) {
            val renderUiImage = uiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
            Box(
                modifier = modifier.then(Modifier.size(20.dp))
            ) {
                UIKitView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        UIImageView().apply {
                            val isTinted = tint != null

                            image = if (isTinted)
                                renderUiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
                            else
                                renderUiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)

                            backgroundColor = UIColor.fromHex(bgColor)

                            if (isTinted) {
                                tintColor = UIColor.fromHex(tint)
                            }

                            opaque = false
                            clipsToBounds = true
                            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                        }
                    },
                    update = { imageView ->
                        val isTinted = tint != null

                        imageView.image = if (isTinted)
                            renderUiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
                        else
                            renderUiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)

                        imageView.backgroundColor = UIColor.fromHex(bgColor)

                        if (isTinted) {
                            imageView.tintColor = UIColor.fromHex(tint)
                        }
                        // If tint becomes null later, we don't touch tintColor; renderingMode=Original keeps the original colors.

                        imageView.opaque = false
                        imageView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                        imageView.clipsToBounds = true
                    }
                )

            }

        }
    }
}

fun UIColor.Companion.fromHex(hex: String): UIColor {
    val cleanHex = hex.removePrefix("#")
    val int = cleanHex.toULong(16)

    return when (cleanHex.length) {
        6 -> { // RRGGBB
            val r = ((int shr 16) and 0xFFu).toDouble() / 255.0
            val g = ((int shr 8) and 0xFFu).toDouble() / 255.0
            val b = (int and 0xFFu).toDouble() / 255.0
            UIColor(red = r, green = g, blue = b, alpha = 1.0)
        }
        8 -> { // AARRGGBB
            val a = ((int shr 24) and 0xFFu).toDouble() / 255.0
            val r = ((int shr 16) and 0xFFu).toDouble() / 255.0
            val g = ((int shr 8) and 0xFFu).toDouble() / 255.0
            val b = (int and 0xFFu).toDouble() / 255.0
            UIColor(red = r, green = g, blue = b, alpha = a)
        }
        else -> clearColor
    }
}

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {

}

actual fun exitApp() {

}

actual object TokenStorage {
    actual fun updateTokenInStorage(token: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            if(token != null) {
                IosCryptoHelper.saveToKeychain(KEY_FCM_TOKEN, token)
            }
        }
    }
}

actual inline fun logMessage(tag: String, message: () -> String) {
    NSLog("[$tag] ${message()}")
}

actual fun generateRandomId(): String {
    return NSUUID().UUIDString()
}

actual fun getCurrentTime() : Long{
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun convertTimeToDateString(time : Long) : String{
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = "dd/MM/yyyy HH:mm"

    // Convert milliseconds (Long) to NSDate
    val date = NSDate.dateWithTimeIntervalSince1970(time.toDouble() / 1000)

    return dateFormatter.stringFromDate(date)
}

actual fun getRandomIdForNotification() : String {
    return "Noti-" + NSUUID().UUIDString()
}

actual suspend fun getImageBytesFromDrawable(name: String): ByteArray?{
    // Load UIImage from the main bundle
    val image = UIImage.imageNamed(name) ?: return null

    // Convert to PNG data
    val data = UIImagePNGRepresentation(image) ?: return null

    // Convert NSData to ByteArray
    val length = data.length.toInt()
    val bytes = ByteArray(length)
    memScoped {
        val buffer = bytes.refTo(0).getPointer(this)
        data.getBytes(buffer, length.toULong())
    }

    return bytes
}

private fun getCacheDir(): String {
    return NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true).first() as String
}

actual fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        components {
            setupDefaultComponents()
        }
        interceptor {
            // cache 32MB bitmap
            bitmapMemoryCacheConfig {
                maxSize(32 * 1024 * 1024) // 32MB
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
                directory(getCacheDir().toPath().resolve("image_cache"))
                maxSizeBytes(512L * 1024 * 1024) // 512MB
            }
        }
    }
}

class IOSNavigationHandlerWithStack( var screenHandler :MutableState<IosScreen>) : NavigationHandler{
    private val screenStack = mutableStateListOf<IosScreen>()

    val currentScreen: IosScreen
        get() = screenStack.lastOrNull() ?: IosScreen.SignInScreen

    override fun navigateTo(screen: String) {
        screenStack.add(screen.toIosScreen())
        screenHandler.value = screen.toIosScreen()
    }

    override fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeLast()
            screenHandler.value = currentScreen
        }
    }

    override fun getCurrentRoute(): String? {
        return (screenStack.lastOrNull() ?: IosScreen.SignInScreen).toString()
    }

    fun resetTo(screen: IosScreen) {
        screenStack.clear()
        screenStack.add(screen)
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64String(): String =
    Base64.encode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.base64ToByteArray(): ByteArray =
    Base64.decode(this)

fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.dataWithBytes(
            bytes = pinned.addressOf(0),
            length = this.size.toULong()
        )
    }
}

actual fun onPushNotificationReceived(data: Map<String, Any?>) {
    logMessage("onPushNotificationReceived", { data.toString() })

    val fcmToken = data[KEY_FCM_TOKEN] as? String
    val userId = data[KEY_USER_ID] as? String
    val avatar = data[KEY_AVATAR] as? String
    val email = data[KEY_EMAIL] as? String
    val title = data[REMOTE_MSG_TITLE] as? String ?: "New Notification"
    val body = data[REMOTE_MSG_BODY] as? String ?: "You have a notification"

    val content = UNMutableNotificationContent().apply {
        this.setTitle(title)
        this.setBody(body)
        this.setSound(UNNotificationSound.defaultSound())
    }

    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = NSUUID().UUIDString,
        content = content,
        trigger = null // immediate
    )

    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
        if (error != null) {
            logMessage("iOS Notification", { "Failed to schedule notification: $error" })
        } else {
            logMessage("iOS Notification", { "Notification scheduled" })
        }
    }
}

fun getBottomSafeAreaInset(): Dp {
	val candidateWindows: List<UIWindow> = UIApplication.sharedApplication.connectedScenes
		.filterIsInstance<UIWindowScene>()
		.flatMap { scene ->
			(scene.windows as? List<UIWindow>) ?: emptyList()
		}

	// Prefer key window, else fall back to the first window
	val window = candidateWindows.firstOrNull { it.isKeyWindow() } ?: candidateWindows.firstOrNull()

	val bottomInsetPoints = window?.safeAreaInsets?.useContents { bottom } ?: 0.0
	val insetDp = bottomInsetPoints.dp
	// Typical iPhone home indicator inset is ~34pt; cap conservatively
	return insetDp.coerceIn(0.dp, 36.dp)
}

const val serviceName: String = "iosLocalStorage"
@OptIn(ExperimentalSettingsImplementation::class)
actual val settings: Settings? = KeychainSettings(serviceName)

@Composable
actual fun rememberNavigationHandler(navController: Any): NavigationHandler {
    return remember {
        object : NavigationHandler {
            override fun navigateTo(route: String) { /* no-op placeholder on iOS */ }
            override fun navigateBack() { /* no-op placeholder on iOS */ }
            override fun getCurrentRoute(): String? = null
        }
    }
}

@Composable
actual fun <T : Any> platformViewModel(key: String?, factory: () -> T): T {
    // iOS: just remember an instance; there’s no default ViewModelStore
    return remember(key) { factory() }
}

actual fun getUriStringFromLocalPath(localPath: String): String {
    val url = NSURL.fileURLWithPath(localPath)
    return url.absoluteString ?: ""
}

actual fun getAppVersion(): String {
    return NSBundle.mainBundle
        .objectForInfoDictionaryKey("CFBundleShortVersionString")
        ?.toString() ?: ""
}

actual fun generateQrImage(content: String): ImageBitmap {
    // Minimal stub – real QR generation requires a third-party library on iOS
    return ImageBitmap(1, 1)
}

actual object AppConfig {
    actual val twoFAApiKey: String =
        NSBundle.mainBundle
            .objectForInfoDictionaryKey("APP_SCRIPT_FOR_2FA_AUTHENTICATION_API_KEY")
            ?.toString() ?: ""
    actual val supabaseApiKey: String =
        NSBundle.mainBundle
            .objectForInfoDictionaryKey("SUPABASE_API_KEY")
            ?.toString() ?: ""
    actual val ipInfoApiKey: String =
        NSBundle.mainBundle
            .objectForInfoDictionaryKey("IPINFO_API_KEY")
            ?.toString() ?: ""
}