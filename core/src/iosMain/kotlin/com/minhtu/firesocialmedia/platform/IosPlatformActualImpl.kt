package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.core.domain.entity.home.deeplinks.ShareApp
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.core.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFARequest
import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import io.ktor.client.call.body
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.asset
import platform.AVFoundation.currentItem
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
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
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
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
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.QuartzCore.CAGradientLayer
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

actual fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance, type : String): String {
    try {
        val body = buildJsonObject {
            putJsonObject(Constants.REMOTE_MSG_DATA) {
                put(Constants.KEY_FCM_TOKEN, JsonPrimitive(sender.token))
                put(Constants.KEY_USER_ID, JsonPrimitive(sender.uid))
                put(Constants.KEY_AVATAR, JsonPrimitive(sender.image))
                put(Constants.KEY_EMAIL, JsonPrimitive(sender.email))
                put(Constants.REMOTE_MSG_TITLE, JsonPrimitive(sender.name))
                put(Constants.REMOTE_MSG_BODY, JsonPrimitive(message))
                put(Constants.REMOTE_MSG_TYPE, JsonPrimitive(type))
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
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val response = KtorProvider.client.post(Constants.APP_SCRIPT_URL + Constants.APP_SCRIPT_ENDPOINT){
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                logMessage("sendMessageToServer", { "Notification sent successfully" })
            } else {
                logMessage("sendMessageToServer",
                    { "Failed to send notification: ${response.status}" })
            }
        } catch (e: Exception) {
            logMessage("sendMessageToServer", { e.message.toString() })
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

class IosImagePicker(
    private val onImagePicked: (String) -> Unit,
    private val onVideoPicked: (String) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    val imagePicker = object : ImagePicker {
        override fun pickImage() {
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                delegate = this@IosImagePicker
            }
            val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
            rootController?.presentViewController(picker, animated = true, completion = null)
        }

        override fun pickVideo() {
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                mediaTypes = listOf("public.movie", "public.video")
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
            val response = KtorProvider.client.request(url) {
                method = HttpMethod.Get
            }
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
        val mediaType = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaType] as? String
        if (mediaType == "public.image") {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            // Handle picked image
            if (image != null) {
                mainScope.launch {
                    val byteArray = withContext(Dispatchers.Default) { image.toByteArray() }
                    if(byteArray != null) {
                        onImagePicked(byteArray.toBase64String())
                    }
                }
            }
        } else if (mediaType == "public.movie" || mediaType == "public.video") {
            val videoUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
            // Handle picked video URL
            if (videoUrl != null) {
                val videoUriString: String = videoUrl.absoluteString ?: ""
                mainScope.launch {
                    onVideoPicked(videoUriString)
                }
            }
        }
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    // Convert UIImage to ByteArray (PNG representation)
    fun UIImage.toByteArray(): ByteArray? {
        val imageData = UIImagePNGRepresentation(this) ?: return null
        return imageData.toByteArray()
    }

    fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            this.getBytes(pinned.addressOf(0))
        }
        return bytes
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
    logMessage("onPushNotificationReceived", { data.toString() })

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

private fun AVPlayer.play() {
    this.performSelector(NSSelectorFromString("play"))
}

private fun AVPlayer.pause() {
    this.performSelector(NSSelectorFromString("pause"))
}

// ── iOS action button design tokens (mirrors Android ActionButtonTokens) ────
private object IosActionButtonTokens {
    const val CIRCLE_SIZE: CGFloat = 52.0
    const val ICON_PADDING: CGFloat = 12.0
    const val STROKE_WIDTH: CGFloat = 1.0
    const val BUTTON_BOTTOM_MARGIN: CGFloat = 20.0
    const val LABEL_FONT_SIZE: CGFloat = 11.0
    // default: frosted white
    const val DEFAULT_ALPHA: CGFloat = 1.0
    const val DEFAULT_BG_ALPHA: CGFloat = 80.0 / 255.0
    const val DEFAULT_STROKE_ALPHA: CGFloat = 60.0 / 255.0
    // active: soft pink #FF6987
    const val ACTIVE_R: CGFloat = 255.0 / 255.0
    const val ACTIVE_G: CGFloat = 105.0 / 255.0
    const val ACTIVE_B: CGFloat = 135.0 / 255.0
    const val ACTIVE_BG_ALPHA: CGFloat = 60.0 / 255.0
    const val ACTIVE_STROKE_ALPHA: CGFloat = 120.0 / 255.0
}

/** Helper NSObject acting as tap target. */
@OptIn(BetaInteropApi::class)
private class IosActionTapWrapper(val action: () -> Unit) : NSObject() {
    @ObjCAction
    fun handleTap() { action() }
}

/** Helper NSObject for play/pause toggle. */
@OptIn(BetaInteropApi::class)
private class PlayPauseTapTarget(val action: () -> Unit) : NSObject() {
    @ObjCAction
    fun handleTap() { action() }
}

/**
 * Build a vertical button group (circle icon + label) placed at the given origin.
 * Returns the UIView and a reference to the inner icon UIImageView for later tint updates.
 */
private fun iosActionButtonView(
    iconName: String,
    label: String,
    x: CGFloat,
    y: CGFloat,
    isActive: Boolean = false,
    onClick: () -> Unit
): Pair<UIView, UIImageView?> {
    val t = IosActionButtonTokens

    val iconColor: UIColor
    val bgColor: UIColor
    val strokeColor: UIColor
    if (isActive) {
        iconColor   = UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.DEFAULT_ALPHA)
        bgColor     = UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.ACTIVE_BG_ALPHA)
        strokeColor = UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.ACTIVE_STROKE_ALPHA)
    } else {
        iconColor   = UIColor(white = 1.0, alpha = 1.0)
        bgColor     = UIColor(white = 1.0, alpha = t.DEFAULT_BG_ALPHA)
        strokeColor = UIColor(white = 1.0, alpha = t.DEFAULT_STROKE_ALPHA)
    }

    val totalH = t.CIRCLE_SIZE + 6.0 + 14.0
    val container = UIView(CGRectMake(x, y, t.CIRCLE_SIZE, totalH))
    container.backgroundColor = UIColor.clearColor

    // Circle
    val circle = UIView(CGRectMake(0.0, 0.0, t.CIRCLE_SIZE, t.CIRCLE_SIZE))
    circle.layer.cornerRadius = t.CIRCLE_SIZE / 2
    circle.layer.borderWidth = t.STROKE_WIDTH
    circle.layer.borderColor = strokeColor.CGColor
    circle.backgroundColor = bgColor
    circle.layer.shadowOpacity = 0.4f
    circle.layer.shadowRadius = 4.0

    // Icon
    val iconImage = UIImage.systemImageNamed(iconName) ?: UIImage.imageNamed(iconName)
    val iconView = UIImageView(CGRectMake(t.ICON_PADDING, t.ICON_PADDING,
        t.CIRCLE_SIZE - t.ICON_PADDING * 2, t.CIRCLE_SIZE - t.ICON_PADDING * 2))
    iconView.image = iconImage
    iconView.tintColor = iconColor
    iconView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
    circle.addSubview(iconView)

    val tap = IosActionTapWrapper(onClick)
    circle.addGestureRecognizer(UITapGestureRecognizer(tap, NSSelectorFromString("handleTap")))
    circle.userInteractionEnabled = true

    // Label
    val labelView = UILabel(CGRectMake(0.0, t.CIRCLE_SIZE + 4.0, t.CIRCLE_SIZE, 14.0))
    labelView.text = label
    labelView.font = UIFont.systemFontOfSize(t.LABEL_FONT_SIZE)
    labelView.textColor = iconColor
    labelView.textAlignment = platform.UIKit.NSTextAlignmentCenter

    container.addSubview(circle)
    container.addSubview(labelView)

    return container to (if (iconImage != null) iconView else null)
}

// ── Inline VideoPlayerView ────────────────────────────────────────────────────
@OptIn(BetaInteropApi::class)
private class InlineVideoPlayerView(
    private val initialUri: String,
    private val onFullscreenClick: () -> Unit
) : UIView(CGRectZero.readValue()) {

    private val playerLayer = AVPlayerLayer()
    val player: AVPlayer = AVPlayer.playerWithURL(NSURL(string = initialUri))
    private var isPlaying = false
    private val playPauseButton = UIButton.buttonWithType(UIButtonTypeSystem)
    private val fullscreenButton = UIButton.buttonWithType(UIButtonTypeSystem)
    private var hideButtonTimer: NSTimer? = null

    private val playPauseTarget = PlayPauseTapTarget { togglePlayPause() }
    private val fullscreenTarget = IosActionTapWrapper { onFullscreenClick() }

    init {
        playerLayer.player = player
        playerLayer.videoGravity = AVLayerVideoGravityResizeAspect
        layer.addSublayer(playerLayer)
        backgroundColor = UIColor.blackColor

        playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
        playPauseButton.backgroundColor = UIColor.clearColor
        playPauseButton.titleLabel?.font = UIFont.systemFontOfSize(36.0)
        playPauseButton.addTarget(
            target = playPauseTarget,
            action = NSSelectorFromString("handleTap"),
            forControlEvents = UIControlEventTouchUpInside
        )
        addSubview(playPauseButton)

        fullscreenButton.setTitle("⛶", forState = UIControlStateNormal)
        fullscreenButton.backgroundColor = UIColor.clearColor
        fullscreenButton.titleLabel?.font = UIFont.systemFontOfSize(22.0)
        fullscreenButton.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
        fullscreenButton.addTarget(
            target = fullscreenTarget,
            action = NSSelectorFromString("handleTap"),
            forControlEvents = UIControlEventTouchUpInside
        )
        addSubview(fullscreenButton)

        val tapGesture = UITapGestureRecognizer(playPauseTarget, NSSelectorFromString("handleTap"))
        addGestureRecognizer(tapGesture)
        userInteractionEnabled = true
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val (w, h) = bounds.useContents { size.width to size.height }
        playerLayer.setFrame(CGRectMake(0.0, 0.0, w, h))
        val btnSize = 60.0
        playPauseButton.setFrame(CGRectMake((w - btnSize) / 2, (h - btnSize) / 2, btnSize, btnSize))
        val fsSize = 36.0
        fullscreenButton.setFrame(CGRectMake(w - fsSize - 8.0, h - fsSize - 8.0, fsSize, fsSize))
    }

    fun updateVideo(newUri: String) {
        val newUrl = NSURL(string = newUri)
        val newItem = AVPlayerItem(newUrl)
        if (player.currentItem?.asset?.isEqual(newItem.asset) == false) {
            player.replaceCurrentItemWithPlayerItem(newItem)
            isPlaying = false
            playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
        }
    }

    private fun showControlsTemporarily() {
        playPauseButton.hidden = false
        fullscreenButton.hidden = false
        hideButtonTimer?.invalidate()
        hideButtonTimer = NSTimer.scheduledTimerWithTimeInterval(3.0, repeats = false) {
            playPauseButton.hidden = true
            fullscreenButton.hidden = true
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            player.pause()
            playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
        } else {
            player.play()
            playPauseButton.setTitle("⏸", forState = UIControlStateNormal)
        }
        isPlaying = !isPlaying
        showControlsTemporarily()
    }
}

@OptIn(BetaInteropApi::class)
@Composable
actual fun VideoPlayer(
    uri: String,
    modifier: Modifier,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)?
) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    val player = remember { AVPlayer.playerWithURL(NSURL(string = uri)) }

    LaunchedEffect(uri) {
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(NSURL(string = uri)))
    }

    UIKitView(
        factory = { InlineVideoPlayerView(uri) { isFullscreen = true } },
        update = { view -> view.updateVideo(uri) },
        modifier = modifier
    )

    if (isFullscreen) {
        IosFullscreenVideoOverlay(
            player = player,
            isLiked = isLiked,
            onDismiss = { isFullscreen = false },
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            commentSheetContent = commentSheetContent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, BetaInteropApi::class)
@Composable
private fun IosFullscreenVideoOverlay(
    player: AVPlayer,
    isLiked: Boolean,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    commentSheetContent: (@Composable (onDismiss: () -> Unit) -> Unit)?
) {
    var showCommentSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val playerLayerRef  = remember { mutableStateOf<AVPlayerLayer?>(null) }
    val likeIconViewRef = remember { mutableStateOf<UIImageView?>(null) }
    val containerRef    = remember { mutableStateOf<UIView?>(null) }

    // Reactively update like icon tint
    LaunchedEffect(isLiked) {
        val iv = likeIconViewRef.value ?: return@LaunchedEffect
        val t = IosActionButtonTokens
        iv.tintColor = if (isLiked)
            UIColor(red = t.ACTIVE_R, green = t.ACTIVE_G, blue = t.ACTIVE_B, alpha = t.DEFAULT_ALPHA)
        else UIColor(white = 1.0, alpha = 1.0)
    }

    // Resize player layer when comment sheet visible
    LaunchedEffect(showCommentSheet) {
        val c = containerRef.value ?: return@LaunchedEffect
        val pLayer = playerLayerRef.value ?: return@LaunchedEffect
        val (cw, ch) = c.bounds.useContents { size.width to size.height }
        val newH = if (showCommentSheet) ch * 0.45 else ch
        pLayer.setFrame(CGRectMake(0.0, 0.0, cw, newH))
    }

    DisposableEffect(Unit) {
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> @Suppress("UNCHECKED_CAST") (scene.windows as? List<UIWindow>) ?: emptyList() }
            .firstOrNull { it.isKeyWindow() }
            ?: return@DisposableEffect onDispose {}

        val (screenW, screenH) = window.bounds.useContents { size.width to size.height }

        val container = UIView(CGRectMake(0.0, 0.0, screenW, screenH))
        container.backgroundColor = UIColor.blackColor
        containerRef.value = container

        // Player layer (fullscreen)
        val pLayer = AVPlayerLayer()
        pLayer.player = player
        pLayer.videoGravity = AVLayerVideoGravityResizeAspect
        pLayer.setFrame(CGRectMake(0.0, 0.0, screenW, screenH))
        playerLayerRef.value = pLayer
        container.layer.addSublayer(pLayer)

        // Bottom gradient scrim
        val bottomGrad = CAGradientLayer()
        val opaqueBlack = UIColor.blackColor.colorWithAlphaComponent(0.70).CGColor
        val clearBlack  = UIColor.clearColor.CGColor
        bottomGrad.colors = listOf(clearBlack, opaqueBlack)
        bottomGrad.setFrame(CGRectMake(0.0, screenH - 160.0, screenW, 160.0))
        container.layer.addSublayer(bottomGrad)

        // Top gradient scrim
        val topGrad = CAGradientLayer()
        topGrad.colors = listOf(UIColor.blackColor.colorWithAlphaComponent(0.47).CGColor, clearBlack)
        topGrad.setFrame(CGRectMake(0.0, 0.0, screenW, 100.0))
        container.layer.addSublayer(topGrad)

        // ── Action column (right side, above bottom) ────────────────────────
        val t = IosActionButtonTokens
        val itemH  = t.CIRCLE_SIZE + 6.0 + 14.0 + t.BUTTON_BOTTOM_MARGIN
        val colX   = screenW - t.CIRCLE_SIZE - 20.0
        val colY   = screenH - itemH * 3 - 100.0

        val commentAction: () -> Unit = if (commentSheetContent != null) {
            { showCommentSheet = true }
        } else onCommentClick

        val (likeView, likeIcon) = iosActionButtonView("like", "Like", colX, colY, isLiked, onLikeClick)
        likeIconViewRef.value = likeIcon
        val (commentView, _) = iosActionButtonView("bubble.right", "Comment", colX, colY + itemH, onClick = commentAction)
        val (shareView, _)   = iosActionButtonView("square.and.arrow.up", "Share", colX, colY + itemH * 2, onClick = onShareClick)

        container.addSubview(likeView)
        container.addSubview(commentView)
        container.addSubview(shareView)

        // ── Close button (top-left) ──────────────────────────────────────────
        val closeTarget = IosActionTapWrapper(onDismiss)
        val closeBtn = UIButton.buttonWithType(UIButtonTypeSystem)
        closeBtn.setTitle("✕", forState = UIControlStateNormal)
        closeBtn.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
        closeBtn.titleLabel?.font = UIFont.systemFontOfSize(22.0)
        closeBtn.setFrame(CGRectMake(16.0, 48.0, 44.0, 44.0))
        closeBtn.addTarget(closeTarget, NSSelectorFromString("handleTap"), UIControlEventTouchUpInside)
        container.addSubview(closeBtn)

        // ── Play/pause tap overlay ───────────────────────────────────────────
        var fsPlaying = false
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        val ppTarget = PlayPauseTapTarget {
            if (fsPlaying) player.pause() else player.play()
            @Suppress("UNUSED_VALUE")
            fsPlaying = !fsPlaying
        }
        container.addGestureRecognizer(UITapGestureRecognizer(ppTarget, NSSelectorFromString("handleTap")))
        container.userInteractionEnabled = true

        window.addSubview(container)

        onDispose {
            containerRef.value = null
            playerLayerRef.value = null
            likeIconViewRef.value = null
            pLayer.player = null
            container.removeFromSuperview()
        }
    }

    // Comment bottom sheet
    if (showCommentSheet && commentSheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
                commentSheetContent { showCommentSheet = false }
                ToastHost()
            }
        }
    }
}

actual fun createCallMessage(message: String, tokenList: ArrayList<String>, sessionId: String, sender: UserInstance, receiver: UserInstance, type: String): String {
    // iOS implementation will be added later
    return ""
}

actual class WebRTCVideoTrack

@Composable
actual fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    isLocalVideoOff : Boolean,
    modifier: Modifier
) {
    // iOS implementation will be added later
    Box(modifier = modifier) {
        Text("WebRTC Video View - iOS implementation coming soon")
    }
}

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

@Composable
actual fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit,
    onVideoPicked: (String) -> Unit
): ImagePicker {
    return remember { IosImagePicker(onImagePicked, onVideoPicked).imagePicker }
}

@Composable
actual fun setupSignInLauncher(
    context: Any?,
    signInViewModel: GoogleSignInHandler,
    platformContext: PlatformContext
) {
    // No-op on iOS for Google Sign-In in this project setup
}

actual fun getUriStringFromLocalPath(localPath: String): String {
    val url = NSURL.fileURLWithPath(localPath)
    return url.absoluteString ?: ""
}

actual suspend fun queryShareApps(text: String): MutableList<ShareApp> {
    // iOS doesn't expose a direct list of share targets; return empty placeholder
    return mutableListOf()
}

actual fun launchShareAppWithDeepLink(app: ShareApp, deepLink: String) {
    // Minimal attempt to open the deep link; fallback is no-op
    val url = NSURL.URLWithString(deepLink)
    if (url != null) {
        UIApplication.sharedApplication.openURL(url)
    }
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

actual suspend fun send2FARequest(request: TwoFARequest): TwoFAResponse {
    return try {
        val requestBody = Json.encodeToString(request)
        val response = KtorProvider.client.post(Constants.APP_SCRIPT_URL + Constants.APP_SCRIPT_2FA_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        if (response.status.isSuccess()) {
            response.body<TwoFAResponse>()
        } else {
            TwoFAResponse(false, "Request failed: ${response.status}")
        }
    } catch (e: Exception) {
        logMessage("send2FARequest") { "Exception: ${e.message}" }
        TwoFAResponse(false, "Exception happened!")
    }
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