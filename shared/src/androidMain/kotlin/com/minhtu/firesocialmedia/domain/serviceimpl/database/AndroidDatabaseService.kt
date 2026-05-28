package com.minhtu.firesocialmedia.domain.serviceimpl.database

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
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
import com.minhtu.firesocialmedia.data.remote.dto.settings.PollDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.SessionItemDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.security.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.core.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.core.utils.Utils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidDatabaseService(
    context: Context,
    private val storageHelper : StorageHelperInterface) : DatabaseService {
    // Never hold a Service context to avoid leaks; keep only applicationContext
    private val appContext: Context = context.applicationContext
    override suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO) {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(appContext)
        val currentFCMToken = secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")
        if (!currentFCMToken.isNullOrEmpty()) {
            if (currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                AndroidDatabaseHelper.saveStringToDatabase(
                    currentUser.uid,
                    DataConstant.USER_PATH,
                    currentFCMToken,
                    DataConstant.TOKEN_PATH
                )
            }
        }
    }

    override suspend fun checkUserExists(email: String): SignInDTO =
        suspendCancellableCoroutine { continuation ->
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child("users")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!continuation.isActive) return
                    val exists = snapshot.children.any {
                        it.getValue(UserDTO::class.java)?.email == email
                    }
                    val result = if (exists) {
                        SignInDTO(true, SignInError.AccountExist.message!!)
                    } else {
                        SignInDTO(true, SignInError.AccountNotExist.message!!)
                    }
                    continuation.resume(result)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!continuation.isActive) return
                    continuation.resume(SignInDTO(false, Constants.LOGIN_ERROR))
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return AndroidDatabaseHelper.saveValueToDatabase(
            id,
            path,
            value,
            externalPath
        )
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        AndroidDatabaseHelper.updateCountValueInDatabase(
            id,
            path,
            externalPath,
            value
        )
    }

    override suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsDTO
    ) {
        storageHelper.deleteNewsFromDatabase(path, new)
    }

    override suspend fun deleteCommentFromDatabase(
        path: String,
        comment: BaseNewsInstance
    ) {
        AndroidDatabaseHelper.deleteCommentFromDatabase(path, comment)
    }

    override suspend fun saveInstanceToDatabase(
        commentId: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        return storageHelper.saveInstanceToDatabase(
            commentId,
            path,
            instance
        )
    }

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return storageHelper.saveNewToDatabase(
            commentId,
            path,
            instance
        )
    }

    override suspend fun getAllUsers(path: String): ArrayList<UserDTO>? {
        // Phase 1: fetch raw list from Firebase (main-thread callback, no network calls)
        val raw = suspendCancellableCoroutine<ArrayList<UserDTO>?> { continuation ->
            val result = ArrayList<UserDTO>()
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference =
                database.getReference().child(DataConstant.USER_PATH)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    result.clear()
                    for (dataSnapshot in snapshot.children) {
                        val user: UserDTO? = dataSnapshot.getValue(UserDTO::class.java)
                        if (user != null) result.add(user)
                    }
                    if (continuation.isActive) {
                        databaseReference.removeEventListener(this)
                        continuation.resume(result)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) {
                        databaseReference.removeEventListener(this)
                        continuation.resume(null)
                    }
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        } ?: return null

        // Phase 2: async resolve each image URL in parallel
        return ArrayList(
            coroutineScope {
                raw.map { user ->
                    async { user.apply { image = resolveMediaUrlAsync(image) } }
                }.awaitAll()
            }
        )
    }

    override suspend fun getUser(userId: String): UserDTO? {
        // Phase 1: fetch raw user from Firebase
        val raw = withTimeout(5000) {
            suspendCoroutine<UserDTO?> { continuation ->
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference()
                    .child(DataConstant.USER_PATH)
                    .child(userId)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot.getValue(UserDTO::class.java))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: async resolve image URL and all embedded group avatars in parallel
        coroutineScope {
            val imageJob = async { resolveMediaUrlAsync(raw.image) }
            val groupJobs = raw.groups.entries.map { (key, summary) ->
                async { key to summary.copy(avatar = resolveMediaUrlAsync(summary.avatar)) }
            }
            raw.image = imageJob.await()
            val resolvedGroups = groupJobs.awaitAll()
            resolvedGroups.forEach { (key, resolved) -> raw.groups[key] = resolved }
        }
        return raw
    }

    override suspend fun getNew(newId: String): NewsDTO? {
        // Phase 1: fetch raw news from Firebase
        val raw = withTimeout(5000) {
            suspendCoroutine<NewsDTO?> { continuation ->
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference()
                    .child(DataConstant.NEWS_PATH)
                    .child(newId)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(snapshot.getValue(NewsDTO::class.java))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: async resolve media URLs
        raw.avatar = resolveMediaUrlAsync(raw.avatar)
        raw.image = resolveMediaUrlAsync(raw.image)
        raw.video = resolveMediaUrlAsync(raw.video)
        return raw
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        // Phase 1: fetch raw users from Firebase
        val raw = withTimeout(5000) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference(path)
            suspendCoroutine<List<UserDTO>?> { continuation ->
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.children
                            .mapNotNull { it.getValue(UserDTO::class.java) }
                            .filter { it.name.contains(name, ignoreCase = true) }
                            .take(5)
                        continuation.resume(users)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        } ?: return null

        // Phase 2: async resolve image URLs in parallel
        return coroutineScope {
            raw.map { user ->
                async { user.copy(image = resolveMediaUrlAsync(user.image)) }
            }.awaitAll()
        }
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO {
        // Phase 1: fetch raw news list from Firebase (sync, no network calls)
        val raw = suspendCancellableCoroutine { continuation ->
            val query = FirebaseDatabase.getInstance()
                .getReference(path)
                .orderByChild("timePosted")
                .let { q ->
                    when {
                        lastTimePosted != null && !lastKey.isNullOrBlank() -> q.endBefore(lastTimePosted, lastKey)
                        lastTimePosted != null -> q.endBefore(lastTimePosted)
                        else -> q
                    }
                }
                .limitToLast(number)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!continuation.isActive) return
                    val newsList = snapshot.children.mapNotNull { it.getValue(NewsDTO::class.java) }
                    if (newsList.isEmpty()) {
                        query.removeEventListener(this)
                        continuation.resume(LatestNewsDTO())
                        return
                    }
                    val sorted = newsList.sortedByDescending { it.timePosted }
                    val oldest = sorted.last()
                    query.removeEventListener(this)
                    continuation.resume(
                        LatestNewsDTO(
                            news = sorted,
                            lastTimePostedValue = if (newsList.size < number) null else oldest.timePosted.toDouble(),
                            lastKeyValue = oldest.id
                        )
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    if (!continuation.isActive) return
                    query.removeEventListener(this)
                    continuation.resume(LatestNewsDTO())
                }
            }
            query.addValueEventListener(listener)
            continuation.invokeOnCancellation { query.removeEventListener(listener) }
        }

        if (raw.news.isNullOrEmpty()) return raw

        // Phase 2: resolve all post media URLs in parallel (one coroutine per post)
        val resolved = coroutineScope {
            raw.news.map { news ->
                async {
                    news.copy(
                        avatar = resolveMediaUrlAsync(news.avatar),
                        image = resolveMediaUrlAsync(news.image),
                        video = resolveMediaUrlAsync(news.video)
                    )
                }
            }.awaitAll()
        }
        return raw.copy(news = resolved)
    }

    override suspend fun getAllComments(
        path: String,
        newsId: String
    ): List<CommentDTO>? {
        // Phase 1: fetch raw comments from Firebase
        val raw = suspendCancellableCoroutine<List<CommentDTO>?> { continuation ->
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(DataConstant.NEWS_PATH)
                .child(newsId)
                .child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!continuation.isActive) return
                    val comments = snapshot.children.mapNotNull { it.getValue(CommentDTO::class.java) }
                    databaseReference.removeEventListener(this)
                    continuation.resume(comments)
                }
                override fun onCancelled(error: DatabaseError) {
                    if (!continuation.isActive) return
                    databaseReference.removeEventListener(this)
                    continuation.resume(null)
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        } ?: return null

        // Phase 2: async resolve media URLs in parallel
        return coroutineScope {
            raw.map { comment ->
                async {
                    comment.copy(
                        avatar = resolveMediaUrlAsync(comment.avatar),
                        image = resolveMediaUrlAsync(comment.image),
                        video = resolveMediaUrlAsync(comment.video)
                    )
                }
            }.awaitAll()
        }
    }

    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>? {
        // Phase 1: fetch raw notifications from Firebase
        val raw = suspendCancellableCoroutine<List<NotificationDTO>?> { continuation ->
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference(DataConstant.USER_PATH)
                .child(currentUserUid)
                .child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = snapshot.children
                        .mapNotNull { it.getValue(NotificationDTO::class.java) }
                    if (continuation.isActive) {
                        databaseReference.removeEventListener(this)
                        continuation.resume(result)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) {
                        databaseReference.removeEventListener(this)
                        continuation.resume(null)
                    }
                }
            }
            databaseReference.addValueEventListener(listener)
            continuation.invokeOnCancellation { databaseReference.removeEventListener(listener) }
        } ?: return null

        // Phase 2: async resolve avatar URLs in parallel
        return coroutineScope {
            raw.map { notification ->
                async {
                    val resolved = notification.copy(avatar = resolveMediaUrlAsync(notification.avatar))
                    logMessage("getAllNotificationsOfUser") { "${resolved.id} isRead: ${resolved.beRead}" }
                    resolved
                }
            }.awaitAll()
        }
    }

    override suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    ) {
        AndroidDatabaseHelper.saveListToDatabase(id, path, value, externalPath)
    }

    override suspend fun downloadImage(image: String, fileName: String): Boolean {
        return AndroidDatabaseHelper.downloadImage(appContext, image, fileName)
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean {
        return storageHelper.updateNewsFromDatabase(
            path,
            newContent,
            newImage,
            newVideo,
            new
        )
    }

    override suspend fun saveSignUpInformation(user: UserDTO): Boolean =
        storageHelper.saveSignUpInformation(user)

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    ) {
        AndroidDatabaseHelper.saveNotificationToDatabase(id, path, instance)
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    ) {
        AndroidDatabaseHelper.deleteNotificationFromDatabase(id, path, notification)
    }

    override suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswerDTO,
        sendOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.sendOfferToFireBase(
            sessionId,
            offer,
            DataConstant.CALL_PATH,
            sendOfferCallBack
        )
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateDTO,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate,
            whichCandidate,
            DataConstant.CALL_PATH,
            sendIceCandidateCallBack
        )
    }

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSessionDTO,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.sendCallSessionToFirebase(
            session,
            DataConstant.CALL_PATH,
            sendCallSessionCallBack
        )
    }

    override suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatus
    ): Boolean {
        return AndroidDatabaseHelper.sendCallStatusToFirebase(
            sessionId,
            status,
            DataConstant.CALL_PATH
        )
    }

    override suspend fun sendWhoEndCall(
        sessionId: String,
        whoEndCall: String
    ): Boolean {
        return AndroidDatabaseHelper.sendWhoEndCall(sessionId, whoEndCall, DataConstant.CALL_PATH)
    }

    override suspend fun deleteCallSession(sessionId: String): Boolean {
        return AndroidDatabaseHelper.deleteCallSession(sessionId, DataConstant.CALL_PATH)
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    ) {
        AndroidDatabaseHelper.observePhoneCall(
            isInCall,
            currentUserId,
            DataConstant.CALL_PATH,
            phoneCallCallBack,
            endCallSession,
            whoEndCallCallBack,
            iceCandidateCallBack
        )
    }

    override fun stopObservePhoneCall() {
        AndroidDatabaseHelper.stopObservePhoneCall()
    }

    override fun stopObservePhoneCallWithoutCheckingInCall() {
        AndroidDatabaseHelper.stopObservePhoneCallWithoutCheckingInCall()
    }

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        return storageHelper.saveGroupAndUserGroups(
            groupRootPath,
            userRootPath,
            userGroupsField,
            groupAvatarsStoragePath,
            group,
            userId
        )
    }

    override suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO> {
        return AndroidDatabaseHelper.getAllGroups(userPath, groupPath, userId)
    }

    override suspend fun fetchGroupInfo(
        groupId: String,
        groupPath: String
    ): GroupDTO? {
        return AndroidDatabaseHelper.fetchGroupInfo(groupId, groupPath)
    }

    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        return storageHelper.saveNewToGroup(
            newsDTO,
            groupId,
            groupPath,
            postsPath,
            imagePath
        )
    }

    override suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateNotificationStatus(
            newStatus,
            groupId,
            userId,
            userPath,
            groupPath,
            notificationStatusPath
        )
    }

    override suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String> {
        return AndroidDatabaseHelper.getAllMembersInGroup(
            groupId,
            groupPath,
            membersPath
        )
    }

    override suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO {
        return AndroidDatabaseHelper.getGroupConfigs(
            userId,
            groupId,
            userPath,
            groupPath
        )
    }

    override suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        return AndroidDatabaseHelper.fetchNotificationState(
            userId,
            groupId,
            userPath,
            groupPath,
            notificationStatusPath
        )
    }

    override suspend fun inviteFriendToGroup(
        friendDto: UserDTO,
        userPath: String,
        notificationPath: String
    ) {
        return AndroidDatabaseHelper.inviteFriendToGroup(
            friendDto,
            userPath,
            notificationPath
        )
    }

    override suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean {
        return AndroidDatabaseHelper.addUserToGroup(
            user,
            group,
            userPath,
            groupPath,
            memberPath,
            memberCountPath
        )
    }

    override suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean {
        return AndroidDatabaseHelper.removeUserFromGroup(
            user,
            group,
            userPath,
            groupPath,
            memberPath,
            memberCountPath
        )
    }

    override suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean {
        return AndroidDatabaseHelper.deleteGroup(
            user,
            group,
            userPath,
            groupPath
        )
    }

    override suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateMemberRole(
            role,
            user,
            group,
            groupPath,
            memberPath
        )
    }

    override suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> {
        val raw = AndroidDatabaseHelper.fetchGroupsByMemberCount(limit, groupPath, memberCountPath)
        return coroutineScope {
            raw.map { group -> async { group.copy(avatar = resolveMediaUrlAsync(group.avatar)) } }.awaitAll()
        }
    }

    override suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    ) {
        AndroidDatabaseHelper.updateIsReadStatusOfNotification(
            userId,
            notificationId,
            userPath,
            notificationPath
        )
    }

    override suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit> {
        return AndroidDatabaseHelper.deleteAllNotifications(
            uid,
            userPath,
            notificationPath
        )
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    ) {
        AndroidDatabaseHelper.observePhoneCallWithoutCheckingInCall(
            currentUserId,
            DataConstant.CALL_PATH,
            phoneCallCallBack,
            endCallSession,
            whoEndCallCallBack,
            iceCandidateCallBack
        )
    }

    override suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswerDTO,
        sendAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.sendAnswerToFireBase(
            sessionId,
            answer,
            DataConstant.CALL_PATH,
            sendAnswerCallBack
        )
    }

    override suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.updateAnswerInFirebase(
            sessionId,
            updateContent,
            updateField,
            DataConstant.CALL_PATH,
            updateAnswerCallBack
        )
    }

    override suspend fun clearAnswerInFirebase(
        sessionId: String
    ) = suspendCancellableCoroutine { continuation ->
        AndroidDatabaseHelper.clearAnswerInFirebase(
            sessionId,
            DataConstant.CALL_PATH,
            object : Utils.Companion.BasicCallBack {
                override fun onSuccess() {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onFailure() {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        )
    }

    override suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.updateOfferInFirebase(
            sessionId,
            updateContent,
            updateField,
            DataConstant.CALL_PATH,
            updateOfferCallBack
        )
    }

    override suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String
    ): Boolean? = suspendCancellableCoroutine { continuation ->
        val ref = FirebaseDatabase.getInstance().getReference(callPath)

        ref.get().addOnSuccessListener { snapshot ->
            var isBusy = false
            for (session in snapshot.children) {
                val sessionCalleeId = session.child("calleeId").getValue(String::class.java)
                val sessionCallerId = session.child("callerId").getValue(String::class.java)
                if (sessionCalleeId == calleeId || sessionCallerId == calleeId) {
                    isBusy = true
                    break
                }
            }
            if (continuation.isActive) continuation.resume(!isBusy)
        }.addOnFailureListener {
            if (continuation.isActive) continuation.resume(false)
        }
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (answer: OfferAnswerDTO) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        AndroidDatabaseHelper.observeAnswerFromCallee(
            sessionId,
            DataConstant.CALL_PATH,
            answerCallBack,
            rejectCallBack
        )
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: Utils.Companion.CallStatusCallBack
    ) {
        AndroidDatabaseHelper.observeCallStatus(
            sessionId,
            DataConstant.CALL_PATH,
            callStatusCallBack
        )
    }

    override suspend fun cancelObserveAnswerFromCallee(sessionId: String, callPath: String) {
        AndroidDatabaseHelper.cancelObserveAnswerFromCallee(
            sessionId,
            callPath
        )
    }

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (iceCandidate: IceCandidateDTO) -> Unit
    ) {
        AndroidDatabaseHelper.observeIceCandidatesFromCallee(
            sessionId,
            DataConstant.CALL_PATH,
            iceCandidateCallBack
        )
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer: OfferAnswerDTO) -> Unit
    ) {
        AndroidDatabaseHelper.observeVideoCall(
            sessionId,
            DataConstant.CALL_PATH,
            videoCallCallBack
        )
    }

    override suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean,
        userPath: String,
        twoFaEnabledPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateTwoFAEnabledFlagForUser(
            userId,
            twoFAEnabled,
            userPath,
            twoFaEnabledPath
        )
    }

    override suspend fun fetchLoginHistoryList(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    ): List<SessionItemDTO> {
        return AndroidDatabaseHelper.fetchLoginHistoryList(userId, historyPath, loginHistoryPath)
    }

    override fun getLocalSessionId(): String {
        return AndroidDatabaseHelper.getLocalSessionId(appContext)
    }

    override fun clearLocalSessionId() {
        AndroidDatabaseHelper.clearLocalSessionId(appContext)
    }

    override suspend fun deleteLoginSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean {
        return AndroidDatabaseHelper.deleteLoginSession(userId, sessionId, historyPath, loginHistoryPath)
    }

    override suspend fun logoutSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean {
        return AndroidDatabaseHelper.logoutSession(userId, sessionId, historyPath, loginHistoryPath)
    }

    override fun observeSessionStatus(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String,
        onLoggedOut: () -> Unit
    ) {
        AndroidDatabaseHelper.observeSessionStatus(userId, sessionId, historyPath, loginHistoryPath, onLoggedOut)
    }

    override fun stopObserveSessionStatus() {
        AndroidDatabaseHelper.stopObserveSessionStatus()
    }

    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo : IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        AndroidDatabaseHelper.saveLoginActivityInfo(appContext, userId,locationInfo, historyPath, loginHistoryPath)
    }

    override suspend fun updateUserLongField(
        userId: String,
        fieldPath: String,
        value: Long,
        userPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateUserLongField(userId, fieldPath, value, userPath)
    }

    override suspend fun updateUserStringField(
        userId: String,
        fieldPath: String,
        value: String,
        userPath: String
    ): Boolean {
        return AndroidDatabaseHelper.updateUserStringField(userId, fieldPath, value, userPath)
    }

    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean {
        return AndroidDatabaseHelper.updateUserAvatar(userId, imageUri, userPath)
    }

    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean {
        return AndroidDatabaseHelper.updateUserBackground(userId, imageUri, userPath)
    }

    override suspend fun createPoll(
        poll: PollDTO,
        pollPath: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        newsEntry: NewsDTO
    ): Boolean {
        return AndroidDatabaseHelper.createPoll(poll, pollPath, groupPath, groupId, postsPath, newsEntry)
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean {
        return AndroidDatabaseHelper.deletePollFromDatabase(newsId, pollId, groupPath, groupId, postsPath, pollPath, pollVotesPath)
    }

    override suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO? {
        return AndroidDatabaseHelper.fetchPoll(pollId, pollPath)
    }

    override suspend fun loadMyVotes(pollId: String, userId: String, pollVotesPath: String): List<Int> {
        return AndroidDatabaseHelper.loadMyVotes(pollId, userId, pollVotesPath)
    }

    override suspend fun loadAllVoters(pollId: String, pollVotesPath: String): Map<String, List<Int>> {
        return AndroidDatabaseHelper.loadAllVoters(pollId, pollVotesPath)
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>,
        pollPath: String,
        pollVotesPath: String
    ): Boolean {
        return AndroidDatabaseHelper.submitVote(pollId, userId, selectedIndices, previousIndices, pollPath, pollVotesPath)
    }
}