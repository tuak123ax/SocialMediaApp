package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.model.CommentInstance
import com.minhtu.firesocialmedia.data.model.NewsInstance
import com.minhtu.firesocialmedia.data.model.NotificationInstance
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.platform.PermissionManager
import com.minhtu.firesocialmedia.platform.WebRTCVideoTrack
import com.minhtu.firesocialmedia.presentation.signin.SignInState
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.Mockable
import kotlinx.coroutines.flow.MutableStateFlow

@Mockable
interface PlatformContext {
    val auth: AuthService
    val firebase: FirebaseService
    val crypto: CryptoService
    val database : DatabaseService
    val clipboard : ClipboardService
    val audioCall : AudioCallService
    val permissionManager : PermissionManager
}

@Mockable
interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmailAndPassword(email: String, password: String) : Result<Unit>
    fun getCurrentUserUid() : String?
    fun getCurrentUserEmail() : String?
    fun fetchSignInMethodsForEmail(email: String, callback: Utils.Companion.FetchSignInMethodCallback)
    fun sendPasswordResetEmail(email: String, callback: Utils.Companion.SendPasswordResetEmailCallback)
    fun handleSignInGoogleResult(
        credentials: Any,
        callback: Utils.Companion.SignInGoogleCallback
    )
}

@Mockable
interface FirebaseService {
    fun checkUserExists(email: String, callback: (result : SignInState) -> Unit)
}

data class Credentials(val email: String, val password: String)

@Mockable
interface CryptoService {
    fun saveAccount(email: String, password: String)
    suspend fun loadAccount(): Credentials?
    fun clearAccount()
    suspend fun getFCMToken() : String
}

@Mockable
interface DatabaseService {
    suspend fun updateFCMTokenForCurrentUser(currentUser : UserInstance)
    suspend fun saveValueToDatabase(
        id : String,
        path : String,
        value : HashMap<String, Int>,
        externalPath : String,
        callback : Utils.Companion.BasicCallBack
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
                                      callBack: Utils.Companion.SaveSignUpInformationCallBack)
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

    fun sendOfferToFireBase(
        sessionId : String,
        offer: OfferAnswer,
        callPath : String,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    fun sendIceCandidateToFireBase(sessionId : String,
                                   iceCandidate: IceCandidateData,
                                   whichCandidate : String,
                                   callPath : String,
                                   sendIceCandidateCallBack : Utils.Companion.BasicCallBack)
    fun sendCallSessionToFirebase(session: AudioCallSession,
                                  callPath : String,
                                  sendCallSessionCallBack : Utils.Companion.BasicCallBack)

    fun deleteCallSession(
        sessionId: String,
        callPath: String,
        deleteCallBack : Utils.Companion.BasicCallBack
    )

    fun observePhoneCall(
        isInCall : MutableStateFlow<Boolean>,
        currentUserId : String,
        callPath : String,
        phoneCallCallBack : (String,String, String, OfferAnswer) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit)
    fun sendAnswerToFirebase(
        sessionId : String,
        answer: OfferAnswer,
        callPath : String,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String,
        onResult: (Boolean) -> Unit
    )

    fun observeAnswerFromCallee(
        sessionId : String,
        callPath : String,
        answerCallBack : (answer : OfferAnswer) -> Unit
    )

    fun cancelObserveAnswerFromCallee(
        sessionId : String,
        callPath : String
    )

    fun observeIceCandidatesFromCallee(
        sessionId : String,
        callPath : String,
        iceCandidateCallBack: (iceCandidate : IceCandidateData) -> Unit
    )

    fun observeVideoCall(
        sessionId: String,
        callPath : String,
        videoCallCallBack: (offer : OfferAnswer) -> Unit
    )
}

@Mockable
interface ClipboardService {
    fun copy(text: String)
}

interface AudioCallService{
    fun createOffer(onOfferCreated : (offer : OfferAnswer) -> Unit)
    fun createVideoOffer(onOfferCreated : (offer : OfferAnswer) -> Unit)
    fun createAnswer(videoSupport : Boolean, onAnswerCreated : (answer : OfferAnswer) -> Unit)
    fun setRemoteDescription(remoteOffer : OfferAnswer)
    fun addIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int)
    fun startCall()
    fun stopCall()
    fun initialize(onIceCandidateCreated : (iceCandidateData : IceCandidateData) -> Unit,
                   onRemoteVideoTrackReceived: (remoteVideoTrack :WebRTCVideoTrack) -> Unit)
    fun startVideoCall(onStartVideoCall : (videoTrack : WebRTCVideoTrack) -> Unit)
}