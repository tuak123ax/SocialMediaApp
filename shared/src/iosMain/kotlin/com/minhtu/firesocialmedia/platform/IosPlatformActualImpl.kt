package com.minhtu.firesocialmedia.platform

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
import androidx.compose.runtime.remember
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
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.asset
import platform.AVFoundation.currentItem
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
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
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
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

@Composable
actual fun PasswordVisibilityIcon(passwordVisibility: Boolean) {
    val iconName = if (passwordVisibility) "visibility" else "visibility_off"
    val descriptionOfIcon = if(passwordVisibility) "Hide password" else "Show password"
    CrossPlatformIcon(
        icon = iconName,
        backgroundColor = "#FF132026",
        contentDescription = descriptionOfIcon,
        Modifier.size(24.dp)
    )
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
    logMessage("sendMessageToServer", { "request: $request" })
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

@OptIn(BetaInteropApi::class)
@Composable
actual fun VideoPlayer(uri: String, modifier: Modifier) {
    var player: AVPlayer? = remember { null }

    class VideoPlayerView(val uri: String) : UIView(CGRectZero.readValue()) {
        private val playerLayer = AVPlayerLayer()
        private val url = NSURL(string = uri)
        private var isPlaying = false
        private val playPauseButton = UIButton.buttonWithType(UIButtonTypeSystem)

        init {
            player = AVPlayer.playerWithURL(url)
            playerLayer.player = player
            layer.addSublayer(playerLayer)

            setupPlayPauseButton()

            val tapGesture = UITapGestureRecognizer(target = this, action = NSSelectorFromString("togglePlayPause"))
            addGestureRecognizer(tapGesture)
            userInteractionEnabled = true
        }

        private fun setupPlayPauseButton() {
            playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
            playPauseButton.backgroundColor = UIColor.clearColor
            playPauseButton.titleLabel?.font = UIFont.systemFontOfSize(40.0)
            playPauseButton.setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
            playPauseButton.addTarget(
                target = this,
                action = NSSelectorFromString("togglePlayPause"),
                forControlEvents = UIControlEventTouchUpInside
            )
            addSubview(playPauseButton)
        }

        override fun layoutSubviews() {
            super.layoutSubviews()
            playerLayer.frame = bounds

            // Define your button size (width and height)
            val buttonSize = CGSizeMake(60.0, 60.0)
            var buttonWidth = 0.0
            var buttonHeight = 0.0
            buttonSize.useContents {
                buttonWidth = width
                buttonHeight = height
            }

            // Get the width and height of the view bounds directly
            val (width, height) = bounds.useContents {
                this.size.width to this.size.height
            }

            // Calculate the button's top-left position to center it
            val centerX = (width - buttonWidth) / 2
            val centerY = (height - buttonHeight) / 2

            // Set the playPauseButton's frame to center it
            playPauseButton.setFrame(CGRectMake(centerX, centerY, buttonWidth, buttonHeight))
        }

        fun updateVideo(newUri: String) {
            val newUrl = NSURL(string = newUri)
            val newItem = AVPlayerItem(newUrl)
            if (player?.currentItem?.asset?.isEqual(newItem.asset) == false) {
                player.replaceCurrentItemWithPlayerItem(newItem)
                isPlaying = false
            }
        }

        private var hideButtonTimer: NSTimer? = null

        private fun showPlayPauseButtonTemporarily() {
            playPauseButton.hidden = false

            // Invalidate any existing timer so it restarts countdown
            hideButtonTimer?.invalidate()

            // Schedule to hide the button after 3 seconds
            hideButtonTimer = NSTimer.scheduledTimerWithTimeInterval(
                3.0,
                repeats = false
            ) {
                playPauseButton.hidden = true
            }
        }

        @ObjCAction
        fun togglePlayPause() {
            player?.let {
                if (isPlaying) {
                    it.pause() // pause is resolved by default now
                    playPauseButton.setTitle("▶️", forState = UIControlStateNormal)
                } else {
                    it.play()
                    playPauseButton.setTitle("⏸", forState = UIControlStateNormal)
                }
                isPlaying = !isPlaying

                // Show button again and restart hide timer on every toggle
                showPlayPauseButtonTemporarily()
            }
        }
    }

    UIKitView(
        factory = { VideoPlayerView(uri) },
        update = { view -> view.updateVideo(uri) },
        modifier = modifier
    )
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
    modifier: Modifier
) {
    // iOS implementation will be added later
    Box(modifier = modifier) {
        Text("WebRTC Video View - iOS implementation coming soon")
    }
}

@Composable
actual fun rememberNavigationHandler(navController: Any): NavigationHandler {
    val controller = navController as NavHostController
    return remember(controller) {
        object : NavigationHandler {
            override fun navigateTo(route: String) {
                controller.navigate(route)
            }
            override fun navigateBack() {
                controller.popBackStack()
            }
            override fun getCurrentRoute(): String? {
                return controller.currentBackStackEntry?.destination?.route
            }
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
    signInViewModel: SignInViewModel,
    platformContext: PlatformContext
) {
    // No-op on iOS for Google Sign-In in this project setup
}

