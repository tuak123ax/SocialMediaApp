package com.minhtu.firesocialmedia

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.minhtu.firesocialmedia.instance.BaseNewsInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.signin.SignInState
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.ImageLoader
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.russhwolf.settings.Settings

expect class PlatformContext {
    val auth: AuthService
    val firebase: FirebaseService
    val crypto: CryptoService
    val database : DatabaseService
}

interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    fun getCurrentUserUid() : String?
    fun getCurrentUserEmail() : String?
    fun fetchSignInMethodsForEmail(email: String, stateFlow: MutableStateFlow<Pair<Boolean, String>?>)
    fun sendPasswordResetEmail(email: String, stateFlow: MutableStateFlow<Boolean?>)
    fun handleSignInGoogleResult(
        credentials: Any,
        callback: Utils.Companion.SignInGoogleCallback
    )
}

interface FirebaseService {
    fun checkUserExists(email: String, callback: (result : SignInState) -> Unit)
}

data class Credentials(val email: String, val password: String)

interface CryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): Credentials?
    fun clearAccount()
    suspend fun getFCMToken() : String
}

interface DatabaseService {
    suspend fun updateFCMTokenForCurrentUser(currentUser : UserInstance)
    suspend fun saveValueToDatabase(
        id : String,
        path : String,
        value : HashMap<String, Boolean>,
        externalPath : String
    )

    suspend fun updateCountValueInDatabase(
        id : String,
        path : String,
        externalPath : String,
        value : Int
    )

    suspend fun deleteNewsFromDatabase(path : String,
                               new: NewsInstance)

    suspend fun saveInstanceToDatabase(
        id : String,
        path : String,
        instance : NewsInstance,
        liveData :  MutableStateFlow<Boolean?>
    )

    suspend fun saveInstanceToDatabase(
        id : String,
        path : String,
        instance : CommentInstance,
        liveData :  MutableStateFlow<Boolean?>
    )

    fun getAllUsers(path: String, callback: Utils.Companion.GetUserCallback)
    fun getAllNews(path: String, callback: Utils.Companion.GetNewCallback)
    fun getAllComments(path: String, newsId: String, callback: Utils.Companion.GetCommentCallback)
    fun getAllNotificationsOfUser(path: String, currentUserUid: String, callback: Utils.Companion.GetNotificationCallback)
    suspend fun saveListToDatabase(
        id : String,
        path : String,
        value : ArrayList<String>,
        externalPath : String
    )

    fun downloadImage(image: String, fileName: String, onComplete: (Boolean) -> Unit)
    suspend fun updateNewsFromDatabase(
        path : String,
        newContent : String,
        newImage : String,
        new: NewsInstance,
        status: MutableStateFlow<Boolean?>
    )

    suspend fun saveSignUpInformation(user : UserInstance,
                              addInformationStatus : MutableStateFlow<Boolean?>)
    suspend fun saveNotificationToDatabase(
        id : String,
        path : String,
        instance : ArrayList<NotificationInstance>
    )

    suspend fun deleteNotificationFromDatabase(
        id : String,
        path : String,
        notification: NotificationInstance
    )
}

expect fun showToast(message: String)

@Composable
expect fun getIconPainter(icon : String): Painter?

@Composable
expect fun getIconComposable(icon: String, color : String, modifier: Modifier): (@Composable () -> Unit)?

@Composable
fun CrossPlatformIcon(
    icon: String?,
    color : String,
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
                modifier = modifier.size(20.dp),
                contentScale = contentScale,
                colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
            )
        } else {
            // iOS (or platforms using composable fallback)
            val iconComposable = getIconComposable(icon, color, modifier)
            if (iconComposable != null) {
                iconComposable()
            }
        }
    }
}


@Composable
expect fun CommonBackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable
expect fun PasswordVisibilityIcon(passwordVisibility : Boolean)

expect fun exitApp()

expect fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance): String

expect fun sendMessageToServer(request: String)

expect object TokenStorage {
    fun updateTokenInStorage(token: String?)
}

expect fun logMessage(tag: String, message :String)

expect fun generateRandomId(): String

expect fun getCurrentTime() : Long

expect fun convertTimeToDateString(time : Long) : String

expect fun getRandomIdForNotification() : String

expect suspend fun getImageBytesFromDrawable(name: String): ByteArray?

interface ImagePicker {
    @Composable
    fun RegisterLauncher(hideLoading : () -> Unit)
    fun pickImage()
    suspend fun loadImageBytes(uri: String): ByteArray?
    @Composable
    fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier)
}

expect fun generateImageLoader(): ImageLoader

expect object MainApplication {
    @Composable
    fun MainApp(context: Any)
}

@Composable
expect fun SetUpNavigation(context : Any)

interface SignInLauncher {
    fun launchGoogleSignIn()
}

object SharedPushHandler {
    fun handlePushNotification(payload: Map<String, Any?>) {
        onPushNotificationReceived(payload)
    }
}

expect fun onPushNotificationReceived(data: Map<String, Any?>)

expect val settings: Settings?