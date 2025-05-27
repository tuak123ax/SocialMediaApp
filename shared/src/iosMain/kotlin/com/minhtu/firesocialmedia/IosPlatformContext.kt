package com.minhtu.firesocialmedia

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.auth.IosAuthService
import com.minhtu.firesocialmedia.services.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.services.crypto.IosCryptoService
import com.minhtu.firesocialmedia.services.database.IosDatabaseService
import com.minhtu.firesocialmedia.services.firebase.IosFirebaseService
import com.minhtu.firesocialmedia.services.notification.KtorProvider
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
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
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIImageView
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewContentMode
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import platform.UIKit.safeAreaInsets
import platform.UIKit.*

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

actual class PlatformContext(
) {
    actual val auth: AuthService = IosAuthService()
    actual val firebase: FirebaseService = IosFirebaseService()
    actual val crypto: CryptoService = IosCryptoService()
    actual val database: DatabaseService = IosDatabaseService()
}

@Composable
actual fun getIconPainter(icon: String): Painter? {
    return null // iOS will use the composable fallback instead
}

@Composable
actual fun getIconComposable(icon: String, color : String, modifier : Modifier): (@Composable () -> Unit)? {
    val uiImage = UIImage.imageNamed(icon)
    return {
        if(uiImage != null) {
            Box(
                modifier = modifier
            ) {
                UIKitView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        UIImageView().apply {
                            image = uiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
                            backgroundColor = UIColor.fromHex(color)
                            opaque = false
                            clipsToBounds = true
                            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                        }
                    },
                    update = { imageView ->
                        imageView.image = uiImage.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
                        imageView.backgroundColor = UIColor.fromHex(color)
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
        else -> UIColor.clearColor
    }
}

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {

}

@Composable
actual fun PasswordVisibilityIcon(passwordVisibility: Boolean) {
    val iconName = if (passwordVisibility) "visibility" else "visibility_off"
    val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
    CrossPlatformIcon(icon = iconName, color = "#FF132026", contentDescription = descriptionOfIcon, Modifier.size(24.dp))
}

actual fun exitApp() {

}

actual fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance): String {
    try {
        val body = buildJsonObject {
            putJsonObject(Constants.REMOTE_MSG_DATA) {
                put(Constants.KEY_FCM_TOKEN, JsonPrimitive(sender.token))
                put(Constants.KEY_USER_ID, JsonPrimitive(sender.uid))
                put(Constants.KEY_AVATAR, JsonPrimitive(sender.image))
                put(Constants.KEY_EMAIL, JsonPrimitive(sender.email))
                put(Constants.REMOTE_MSG_TITLE, JsonPrimitive(sender.name))
                put(Constants.REMOTE_MSG_BODY, JsonPrimitive(message))
            }
            putJsonArray(Constants.REMOTE_MSG_TOKENS) {
                for(token in tokenList) {
                    add(JsonPrimitive(token))
                }
            }
        }
        return body.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

actual fun sendMessageToServer(request: String) {
    logMessage("sendMessageToServer", "request: $request")
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val response = KtorProvider.client.post(Constants.APP_SCRIPT_URL + "AKfycbw4JXnBNCl-hoHi2l0_l-Ugp-9icTBWPJVR5PyKqe5o7-JJ-p26yFVpBO8kUZhxtUSzWA/exec"){
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                logMessage("sendMessageToServer","Notification sent successfully")
            } else {
                logMessage("sendMessageToServer","Failed to send notification: ${response.status}")
            }
        } catch (e: Exception) {
            logMessage("sendMessageToServer", e.message.toString())
        }
    }
}

actual object TokenStorage {
    actual fun updateTokenInStorage(token: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            if(token != null) {
                IosCryptoHelper.saveToKeychain(Constants.KEY_FCM_TOKEN, token)
            }
        }
    }
}

actual fun logMessage(tag: String, message: String) {
    NSLog("[$tag] $message")
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

class IosImagePicker(
    private val onImagePicked: (String) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    val imagePicker = object : ImagePicker{
        override fun pickImage() {
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                delegate = this@IosImagePicker
            }
            val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
            rootController?.presentViewController(picker, animated = true, completion = null)
        }

        override suspend fun loadImageBytes(byteString: String): ByteArray? {
            if(byteString.contains("https://")){
                return downloadImageAsByteArray(byteString)
            } else {
                return byteString.base64ToByteArray()
            }
        }

        suspend fun downloadImageAsByteArray(url: String): ByteArray {
            val response = KtorProvider.client.get(url)
            return response.readBytes()
        }

        @Composable
        override fun RegisterLauncher(hideLoading: () -> Unit) {
            // No launcher registration needed in iOS
        }

        @Composable
        override fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier) {
            byteArray?.let { bytes ->
                val imageBitmap = bytes.toImageBitmap()
                imageBitmap?.let { bitmap ->
                    Image(
                        painter = BitmapPainter(bitmap),
                        contentDescription = "Picked image",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                    )
                }
            }
        }
    }

    private val mainScope = MainScope()


    // Called when user picks an image
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true, completion = null)

        if (image != null) {
            mainScope.launch {
                val byteArray = withContext(Dispatchers.Default) { image.toByteArray() }
                if(byteArray != null) {
                    onImagePicked(byteArray.toBase64String())
                }
            }
        }
    }

    // Convert UIImage to ByteArray (PNG representation)
    fun UIImage.toByteArray(): ByteArray? {
        val imageData = UIImagePNGRepresentation(this) ?: return null
        return imageData.toByteArray()
    }

    fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val bytesPointer = this.bytes?.reinterpret<ByteVar>() ?: return ByteArray(0)

        return ByteArray(length) { index ->
            bytesPointer[index]
        }
    }

    // Convert ByteArray to Compose ImageBitmap
    private fun ByteArray.toImageBitmap(): ImageBitmap? {
        return try {
            Image.makeFromEncoded(this).toComposeImageBitmap()
        } catch (e: Exception) {
            println("Image decode failed: ${e.message}")
            null
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
    logMessage("onPushNotificationReceived", data.toString())

    val fcmToken = data[Constants.KEY_FCM_TOKEN] as? String
    val userId = data[Constants.KEY_USER_ID] as? String
    val avatar = data[Constants.KEY_AVATAR] as? String
    val email = data[Constants.KEY_EMAIL] as? String
    val title = data[Constants.REMOTE_MSG_TITLE] as? String ?: "New Notification"
    val body = data[Constants.REMOTE_MSG_BODY] as? String ?: "You have a notification"

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
            logMessage("iOS Notification", "Failed to schedule notification: $error")
        } else {
            logMessage("iOS Notification", "Notification scheduled")
        }
    }
}

fun getBottomSafeAreaInset(): Dp {
    val keyWindow = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()
        ?.windows
        ?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow

    val bottomInset = keyWindow?.safeAreaInsets?.useContents { bottom } ?: 0.0
    return bottomInset.dp
}

const val serviceName: String = "iosLocalStorage"
@OptIn(ExperimentalSettingsImplementation::class)
actual val settings: Settings? = KeychainSettings(serviceName)