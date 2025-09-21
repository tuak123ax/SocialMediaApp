package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.data.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.Mockable
import kotlinx.coroutines.flow.MutableStateFlow

@Mockable
interface DatabaseService {
    suspend fun updateFCMTokenForCurrentUser(currentUser : UserDTO)
    suspend fun checkUserExists(email: String) : SignInDTO
    suspend fun saveValueToDatabase(
        id : String,
        path : String,
        value : HashMap<String, Int>,
        externalPath : String) : Boolean

    suspend fun updateCountValueInDatabase(
        id : String,
        path : String,
        externalPath : String,
        value : Int
    )

    suspend fun deleteNewsFromDatabase(path : String,
                                       new: NewsDTO
    )

    suspend fun deleteCommentFromDatabase(path : String,
                                          comment: BaseNewsInstance
    )

    suspend fun saveInstanceToDatabase(
        id : String,
        path : String,
        instance : BaseNewsInstance
    ) : Boolean

    suspend fun getAllUsers(path: String) : ArrayList<UserDTO>?
    suspend fun getUser(userId: String) : UserDTO?
    suspend fun getNew(newId: String) : NewsDTO?
    suspend fun getLatestNews(number : Int,
                              lastTimePosted : Double?,
                              lastKey: String?,
                              path: String) : LatestNewsDTO
    suspend fun getAllComments(path: String, newsId: String) : List<CommentDTO>?
    suspend fun getAllNotificationsOfUser(path: String, currentUserUid: String) : List<NotificationDTO>?
    suspend fun saveListToDatabase(
        id : String,
        path : String,
        value : ArrayList<String>,
        externalPath : String
    )

    suspend fun downloadImage(image: String, fileName: String) : Boolean
    suspend fun updateNewsFromDatabase(
        path : String,
        newContent : String,
        newImage : String,
        newVideo : String,
        new: NewsDTO) : Boolean

    suspend fun saveSignUpInformation(user : UserDTO) : Boolean
    suspend fun saveNotificationToDatabase(
        id : String,
        path : String,
        instance : ArrayList<NotificationDTO>
    )

    suspend fun deleteNotificationFromDatabase(
        id : String,
        path : String,
        notification: NotificationDTO
    )

    suspend fun sendOfferToFireBase(
        sessionId : String,
        offer: OfferAnswerDTO,
        sendOfferCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun sendIceCandidateToFireBase(sessionId : String,
                                           iceCandidate: IceCandidateDTO,
                                           whichCandidate : String,
                                           sendIceCandidateCallBack : Utils.Companion.BasicCallBack)
    suspend fun sendCallSessionToFirebase(session: AudioCallSessionDTO,
                                          sendCallSessionCallBack : Utils.Companion.BasicCallBack)

    fun sendCallStatusToFirebase(sessionId: String,
                                 status: CallStatus,
                                 sendCallStatusCallBack : Utils.Companion.BasicCallBack)

    suspend fun deleteCallSession(
        sessionId: String,
        deleteCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun observePhoneCall(
        isInCall : MutableStateFlow<Boolean>,
        currentUserId : String,
        phoneCallCallBack : (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateDTO>?) -> Unit)

    suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId : String,
        phoneCallCallBack : (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateDTO>?) -> Unit)

    suspend fun sendAnswerToFirebase(
        sessionId : String,
        answer: OfferAnswerDTO,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun updateAnswerInFirebase(
        sessionId : String,
        updateContent: String,
        updateField : String,
        updateAnswerCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun updateOfferInFirebase(
        sessionId : String,
        updateContent: String,
        updateField : String,
        updateOfferCallBack : Utils.Companion.BasicCallBack
    )

    suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String
    ) : Boolean?

    suspend fun observeAnswerFromCallee(
        sessionId : String,
        answerCallBack : (answer : OfferAnswerDTO) -> Unit,
        rejectCallBack : () -> Unit
    )

    suspend fun observeCallStatus(
        sessionId : String,
        callStatusCallBack : Utils.Companion.CallStatusCallBack
    )

    suspend fun cancelObserveAnswerFromCallee(
        sessionId : String,
        callPath : String
    )

    suspend fun observeIceCandidatesFromCallee(
        sessionId : String,
        iceCandidateCallBack: (iceCandidate : IceCandidateDTO) -> Unit
    )

    suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer : OfferAnswerDTO) -> Unit
    )

    suspend fun searchUserByName(name: String, path: String) : List<UserDTO>?
}