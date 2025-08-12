package com.minhtu.firesocialmedia.domain.serviceimpl.database

import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.data.model.news.CommentInstance
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.signin.SignInState
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.Mockable
import kotlinx.coroutines.flow.MutableStateFlow

@Mockable
interface DatabaseService {
    suspend fun updateFCMTokenForCurrentUser(currentUser : UserInstance)
    fun checkUserExists(email: String, callback: (result : SignInState) -> Unit)
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
                                       new: NewsInstance
    )

    suspend fun deleteCommentFromDatabase(path : String,
                                          comment: CommentInstance
    )

    suspend fun saveInstanceToDatabase(
        id : String,
        path : String,
        instance : NewsInstance,
        liveData : MutableStateFlow<Boolean?>
    )

    suspend fun saveInstanceToDatabase(
        id : String,
        path : String,
        instance : CommentInstance,
        liveData : MutableStateFlow<Boolean?>
    )

    suspend fun getAllUsers(path: String, callback: Utils.Companion.GetUserCallback)
    suspend fun getLatestNews(number : Int,
                              lastTimePosted : Double?,
                              lastKey: String?,
                              path: String,
                              callback: Utils.Companion.GetNewCallback)
    suspend fun getAllComments(path: String, newsId: String, callback: Utils.Companion.GetCommentCallback)
    suspend fun getAllNotificationsOfUser(path: String, currentUserUid: String, callback: Utils.Companion.GetNotificationCallback)
    suspend fun saveListToDatabase(
        id : String,
        path : String,
        value : ArrayList<String>,
        externalPath : String
    )

    suspend fun downloadImage(image: String, fileName: String, onComplete: (Boolean) -> Unit)
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

    suspend fun sendOfferToFireBase(
        sessionId : String,
        offer: OfferAnswer,
        callPath : String,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun sendIceCandidateToFireBase(sessionId : String,
                                           iceCandidate: IceCandidateData,
                                           whichCandidate : String,
                                           callPath : String,
                                           sendIceCandidateCallBack : Utils.Companion.BasicCallBack)
    suspend fun sendCallSessionToFirebase(session: AudioCallSession,
                                          callPath : String,
                                          sendCallSessionCallBack : Utils.Companion.BasicCallBack)

    fun sendCallStatusToFirebase(sessionId: String,
                                         status: CallStatus,
                                         callPath : String,
                                         sendCallStatusCallBack : Utils.Companion.BasicCallBack)

    suspend fun deleteCallSession(
        sessionId: String,
        callPath: String,
        deleteCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun observePhoneCall(
        isInCall : MutableStateFlow<Boolean>,
        currentUserId : String,
        callPath : String,
        phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit)

    suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId : String,
        callPath : String,
        phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit)

    suspend fun sendAnswerToFirebase(
        sessionId : String,
        answer: OfferAnswer,
        callPath : String,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun updateAnswerInFirebase(
        sessionId : String,
        updateContent: String,
        updateField : String,
        callPath : String,
        updateAnswerCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun updateOfferInFirebase(
        sessionId : String,
        updateContent: String,
        updateField : String,
        callPath : String,
        updateOfferCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String,
        onResult: (Boolean) -> Unit
    )

    suspend fun observeAnswerFromCallee(
        sessionId : String,
        callPath : String,
        answerCallBack : (answer : OfferAnswer) -> Unit,
        rejectCallBack : () -> Unit
    )

    suspend fun observeCallStatus(
        sessionId : String,
        callPath : String,
        callStatusCallBack : Utils.Companion.CallStatusCallBack
    )

    suspend fun cancelObserveAnswerFromCallee(
        sessionId : String,
        callPath : String
    )

    suspend fun observeIceCandidatesFromCallee(
        sessionId : String,
        callPath : String,
        iceCandidateCallBack: (iceCandidate : IceCandidateData) -> Unit
    )

    suspend fun observeVideoCall(
        sessionId: String,
        callPath : String,
        videoCallCallBack: (offer : OfferAnswer) -> Unit
    )
}