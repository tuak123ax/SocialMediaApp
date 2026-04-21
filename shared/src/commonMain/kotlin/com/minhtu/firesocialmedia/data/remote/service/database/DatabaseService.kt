package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.remote.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.data.remote.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.SessionItemDTO
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.utils.Utils
import io.mockative.Mockable
import kotlinx.coroutines.flow.MutableStateFlow

@Mockable
interface DatabaseService {
    suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO)
    suspend fun checkUserExists(email: String): SignInDTO
    suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean

    suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    )

    suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsDTO
    )

    suspend fun deleteCommentFromDatabase(
        path: String,
        comment: BaseNewsInstance
    )

    suspend fun saveInstanceToDatabase(
        commentId: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean

    suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean

    suspend fun getAllUsers(path: String): ArrayList<UserDTO>?
    suspend fun getUser(userId: String): UserDTO?
    suspend fun getNew(newId: String): NewsDTO?
    suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO

    suspend fun getAllComments(path: String, newsId: String): List<CommentDTO>?
    suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>?

    suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    )

    suspend fun downloadImage(image: String, fileName: String): Boolean
    suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean

    suspend fun saveSignUpInformation(user: UserDTO): Boolean
    suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    )

    suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    )

    suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswerDTO,
        sendOfferCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateDTO,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun sendCallSessionToFirebase(
        session: AudioCallSessionDTO,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatus
    ): Boolean

    suspend fun deleteCallSession(sessionId: String): Boolean

    suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    )

    suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    )

    suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswerDTO,
        sendAnswerCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun clearAnswerInFirebase(
        sessionId: String
    )

    suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String
    ): Boolean?

    suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (answer: OfferAnswerDTO) -> Unit,
        rejectCallBack: () -> Unit
    )

    suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: Utils.Companion.CallStatusCallBack
    )

    suspend fun cancelObserveAnswerFromCallee(
        sessionId: String,
        callPath: String
    )

    suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (iceCandidate: IceCandidateDTO) -> Unit
    )

    suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer: OfferAnswerDTO) -> Unit
    )

    suspend fun searchUserByName(name: String, path: String): List<UserDTO>?
    suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean
    fun stopObservePhoneCall()

    /** Removes only the observer from [observePhoneCallWithoutCheckingInCall]. Use when call ends in service so the app's incoming-call observer is not removed. */
    fun stopObservePhoneCallWithoutCheckingInCall()
    suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean

    suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO>

    suspend fun fetchGroupInfo(groupId: String, groupPath: String): GroupDTO?
    suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean

    suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean

    suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String>

    suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO

    suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean

    suspend fun inviteFriendToGroup(
        friendDto: UserDTO,
        userPath: String,
        notificationPath: String
    )

    suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean

    suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean

    suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean

    suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean

    suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO>

    suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    )

    suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit>

    suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean,
        userPath: String,
        twoFaEnabledPath: String
    ): Boolean

    suspend fun fetchLoginHistoryList(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    ): List<SessionItemDTO>

    fun getLocalSessionId(): String

    suspend fun saveLoginActivityInfo(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    )
}