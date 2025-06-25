package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.model.CommentInstance
import com.minhtu.firesocialmedia.data.model.NewsInstance
import com.minhtu.firesocialmedia.data.model.NotificationInstance
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.presentation.signin.SignInState
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow

expect class PlatformContext {
    val auth: AuthService
    val firebase: FirebaseService
    val crypto: CryptoService
    val database : DatabaseService
    val clipboard : ClipboardService
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
        value : HashMap<String, Int>,
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

    suspend fun deleteCommentFromDatabase(path : String,
                                       comment: CommentInstance)

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
        newVideo : String,
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

interface ClipboardService {
    fun copy(text: String)
}