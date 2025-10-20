package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.russhwolf.settings.Settings
import com.seiko.imageloader.ImageLoader
import kotlin.math.roundToInt

expect fun showToast(message: String)

@Composable
expect fun getIconPainter(icon : String): Painter?

@Composable
expect fun getIconComposable(icon: String, bgColor : String, tint : String?, modifier: Modifier): (@Composable () -> Unit)?

@Composable
fun CrossPlatformIcon(
    icon: String?,
    backgroundColor : String,
    contentDescription: String? = null,
    modifier: Modifier,
    tint: Color = Color.Unspecified,
    contentScale: ContentScale = ContentScale.Fit
) {
    if(icon != null) {
        val iconPainter = getIconPainter(icon)

        if (iconPainter != null) {
            // Android (or other platforms that support Painter)
            Image(
                painter = iconPainter,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
            )
        } else {
            // iOS (or platforms using composable fallback)
            val iconComposable = getIconComposable(
                icon,
                backgroundColor,
                if(tint != Color.Unspecified) tint.toHex() else null,
                modifier)
            if (iconComposable != null) {
                iconComposable()
            }
        }
    }
}

fun Color.toHex(): String {
    if (this == Color.Unspecified) return "#FFFFFFFF" // fallback

    val alpha = (alpha * 255).roundToInt().coerceIn(0, 255)
    val red = (red * 255).roundToInt().coerceIn(0, 255)
    val green = (green * 255).roundToInt().coerceIn(0, 255)
    val blue = (blue * 255).roundToInt().coerceIn(0, 255)

    return "#" +
            alpha.toString(16).padStart(2, '0').uppercase() +
            red.toString(16).padStart(2, '0').uppercase() +
            green.toString(16).padStart(2, '0').uppercase() +
            blue.toString(16).padStart(2, '0').uppercase()
}


@Composable
expect fun CommonBackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable
expect fun PasswordVisibilityIcon(passwordVisibility : Boolean)

expect fun exitApp()

expect fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance, type : String): String

expect fun createCallMessage(message: String, tokenList : ArrayList<String>, sessionId : String, sender : UserInstance, receiver : UserInstance, type : String) : String

expect fun sendMessageToServer(request: String)

expect object TokenStorage {
    fun updateTokenInStorage(token: String?)
}

expect inline fun logMessage(tag: String, message: () -> String)

expect fun generateRandomId(): String

expect fun getCurrentTime() : Long

expect fun convertTimeToDateString(time : Long) : String

expect fun getRandomIdForNotification() : String

expect suspend fun getImageBytesFromDrawable(name: String): ByteArray?

expect fun generateImageLoader(): ImageLoader

expect object MainApplication {
    @Composable
    fun MainApp(context: Any, platformContext: PlatformContext)

    @Composable
    fun MainAppFromNotification(context: Any,
                                platformContext: PlatformContext,
                                sessionId : String?,
                                callerId : String?,
                                calleeId : String?)
}

@Composable
expect fun SetUpNavigation(context : Any,
                           platformContext: PlatformContext)

@Composable
expect fun SetUpNavigation(context : Any,
                           platformContext: PlatformContext,
                           sessionId : String?,
                           callerId : String?,
                           calleeId : String?)

object SharedPushHandler {
    fun handlePushNotification(payload: Map<String, Any?>) {
        onPushNotificationReceived(payload)
    }
}

expect fun onPushNotificationReceived(data: Map<String, Any?>)

expect val settings: Settings?

@Composable
expect fun VideoPlayer(uri: String, modifier: Modifier = Modifier)

expect class WebRTCVideoTrack

@Composable
expect fun WebRTCVideoView(
    localTrack: WebRTCVideoTrack?,
    remoteTrack: WebRTCVideoTrack?,
    modifier: Modifier
)

// Platform helpers for common navigation
@Composable
expect fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit,
    onVideoPicked: (String) -> Unit
): ImagePicker

@Composable
expect fun setupSignInLauncher(
    context: Any?,
    signInViewModel: SignInViewModel,
    platformContext: PlatformContext
)

@Composable
expect fun rememberNavigationHandler(navController: Any): com.minhtu.firesocialmedia.utils.NavigationHandler

@Composable
expect fun <T : Any> platformViewModel(key: String? = null, factory: () -> T): T