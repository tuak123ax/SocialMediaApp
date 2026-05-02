package com.minhtu.firesocialmedia.domain.serviceimpl.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import com.minhtu.firesocialmedia.constants.Constants
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
import com.minhtu.firesocialmedia.data.remote.dto.notification.fromMap
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toCommentDTO
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toNewsDTO
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toUserDTO
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

class IosDatabaseService() : DatabaseService {
    override suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO) {
        val currentFCMToken = IosCryptoHelper.getFromKeychain(Constants.KEY_FCM_TOKEN) ?: ""
        if(currentFCMToken.isNotEmpty()) {
            if(currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                IosDatabaseHelper.saveStringToDatabase(currentUser.uid, DataConstant.USER_PATH, currentFCMToken, DataConstant.TOKEN_PATH)
            }
        }
    }

    override suspend fun checkUserExists(email: String): SignInDTO = suspendCancellableCoroutine{ continuation ->
        val database = FIRDatabase.database()
        val ref = database.reference().child("users")

        ref.observeEventType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot: FIRDataSnapshot? ->
                if (snapshot == null || !snapshot.exists()) {
                    if(continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED), onCancellation = {})
                    return@observeEventType
                }

                val children = snapshot.children

                var existed = false

                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val value = child.value as? Map<*,*> ?: continue
                    try {
                        val user = value.toUserDTO()
                        if (user.email == email) {
                            if(continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_EXISTED), onCancellation = {})
                            existed = true
                            break
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }

                if (!existed) {
                    if(continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED), onCancellation = {})
                }
            },
            withCancelBlock = { error ->
                if(continuation.isActive) continuation.resume(SignInDTO(false, Constants.LOGIN_ERROR), onCancellation = {})
            }
        )
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String
    ): Boolean {
        return IosDatabaseHelper.saveValueToDatabase(id,
            path, value, externalPath)
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        IosDatabaseHelper.updateCountValueInDatabase(id,
            path,
            externalPath,
            value)
    }

    override suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsDTO
    ) {
        IosDatabaseHelper.deleteNewsFromDatabase(path, new)
    }

    override suspend fun deleteCommentFromDatabase(
        path: String,
        comment: BaseNewsInstance
    ) {
        IosDatabaseHelper.deleteCommentFromDatabase(path, comment)
    }

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        return IosDatabaseHelper.saveInstanceToDatabase(
            id,
            path,
            instance)
    }

    override suspend fun getAllUsers(path: String): ArrayList<UserDTO>? {
        val rawList = suspendCancellableCoroutine<ArrayList<UserDTO>?> { continuation ->
            val result = ArrayList<UserDTO>()
            val databaseReference = FIRDatabase.database().reference().child(path)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        result.clear()
                        val children = snapshot.children
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue

                            try {
                                val user = value.toUserDTO()
                                result.add(user)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (continuation.isActive) continuation.resume(result, onCancellation = {})
                    } else {
                        if (continuation.isActive) continuation.resume(null, onCancellation = {})
                    }
                }
            )
        } ?: return null

        return coroutineScope {
            rawList.map { user ->
                async {
                    if (user.image.isNotEmpty()) user.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(user.image))
                    user
                }
            }.awaitAll().let { ArrayList(it) }
        }
    }

    override suspend fun getUser(userId: String): UserDTO? {
        val rawUser = suspendCancellableCoroutine<UserDTO?> { continuation ->
            val database = FIRDatabase.database()
            val databaseReference = database.reference()
                .child(DataConstant.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *> ?: null
                        if (value != null) {
                            try {
                                val user = value.toUserDTO()
                                continuation.resume(user) {}
                            } catch (_: Exception) {
                                continuation.resume(null) {}
                            }
                        } else {
                            continuation.resume(null) {}
                        }
                    } else {
                        continuation.resume(null) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        // Phase 2: resolve user avatar + all group avatars in parallel
        if (rawUser.image.isNotEmpty()) rawUser.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawUser.image))
        coroutineScope {
            rawUser.groups.entries.map { (groupId, summary) ->
                async {
                    if (summary.avatar.isNotEmpty()) {
                        val resolved = SupabaseStorageHelper.resolveMediaUrlAsync(summary.avatar)
                        rawUser.groups[groupId] = summary.copy(avatar = resolved)
                    }
                }
            }.awaitAll()
        }
        return rawUser
    }

    override suspend fun getNew(newId: String): NewsDTO? {
        val rawNews = suspendCancellableCoroutine<NewsDTO?> { continuation ->
            val database = FIRDatabase.database()
            val databaseReference = database.reference()
                .child(DataConstant.NEWS_PATH)
                .child(newId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val rawValue = snapshot.value as? Map<*, *> ?: null
                        if (rawValue != null) {
                            try {
                                // Convert Map<*, *> to Map<String, Any?>
                                val value = rawValue.entries.associate {
                                    (it.key as? String) to it.value
                                }.filterKeys { it != null } as Map<String, Any?>

                                val news = value.toNewsDTO()
                                continuation.resume(news) {}
                            } catch (_: Exception) {
                                continuation.resume(null) {}
                            }
                        } else {
                            continuation.resume(null) {}
                        }
                    } else {
                        continuation.resume(null) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        // Phase 2: resolve avatar, image, video
        if (rawNews.avatar.isNotEmpty()) rawNews.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(rawNews.avatar)
        if (rawNews.image.isNotEmpty()) rawNews.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(rawNews.image))
        if (rawNews.video.isNotEmpty()) rawNews.updateVideo(SupabaseStorageHelper.resolveMediaUrlAsync(rawNews.video))
        return rawNews
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO {
        val raw = suspendCancellableCoroutine<LatestNewsDTO> { continuation ->
            val query = FIRDatabase.database()
                .referenceWithPath(path)
                .queryOrderedByChild("timePosted")
                .let { base ->
                    if (lastTimePosted != null && lastKey != null) {
                        base.queryEndingBeforeValue(lastTimePosted, childKey = lastKey)
                    } else base
                }
                .queryLimitedToLast(number.toULong())

            query.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    val enumerator = snapshot?.children
                    val newsList = mutableListOf<NewsDTO>()
                    if (enumerator != null) {
                        while (true) {
                            val child = enumerator.nextObject() as? FIRDataSnapshot ?: break
                            val raw = child.value as? Map<*, *> ?: continue
                            val value = raw.entries
                                .associate { (k, v) -> (k as? String) to v }
                                .filterKeys { it != null } as Map<String, Any?>
                            try {
                                newsList.add(value.toNewsDTO())
                            } catch (_: Exception) {
                            }
                        }
                    }

                    if (newsList.isNotEmpty()) {
                        val sorted = newsList.sortedByDescending { it.timePosted }
                        val oldest = sorted.last()
                        if (continuation.isActive) continuation.resume(LatestNewsDTO(
                            sorted,
                            if (newsList.size < number) null else oldest.timePosted.toDouble(),
                            oldest.id
                        ), onCancellation = {})
                    } else {
                        if (continuation.isActive) continuation.resume(
                            LatestNewsDTO(emptyList(), null, null), onCancellation = {}
                        )
                    }
                }
            ) { _ ->
                continuation.resume(LatestNewsDTO(null, null, null), onCancellation = {})
            }
        }

        // Phase 2: resolve avatar, image, video for every post in parallel
        val resolvedNews = raw.news?.let { list ->
            coroutineScope {
                list.map { news ->
                    async {
                        if (news.avatar.isNotEmpty()) news.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(news.avatar)
                        if (news.image.isNotEmpty()) news.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(news.image))
                        if (news.video.isNotEmpty()) news.updateVideo(SupabaseStorageHelper.resolveMediaUrlAsync(news.video))
                        news
                    }
                }.awaitAll()
            }
        }
        return LatestNewsDTO(resolvedNews, raw.lastTimePostedValue, raw.lastKeyValue)
    }

    override suspend fun getAllComments(
        path: String,
        newsId: String
    ): List<CommentDTO>? {
        val rawList = suspendCancellableCoroutine<List<CommentDTO>?> { continuation ->
            val result = mutableListOf<CommentDTO>()
            val databaseReference = FIRDatabase
                .database()
                .reference()
                .child(DataConstant.NEWS_PATH)
                .child(newsId)
                .child(path)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        result.clear()
                        val children = snapshot.children
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val rawValue = child.value as? Map<*, *> ?: continue

                            // Safely cast Map<*, *> to Map<String, Any?>
                            val value = rawValue.entries.associate {
                                (it.key as? String) to it.value
                            }.filterKeys { it != null } as Map<String, Any?>

                            try {
                                val comment = value.toCommentDTO()
                                result.add(comment)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (continuation.isActive) continuation.resume(ArrayList(result), onCancellation = {})
                    } else {
                        if (continuation.isActive) continuation.resume(null, onCancellation = {})
                    }
                }
            )
        } ?: return null

        return coroutineScope {
            rawList.map { comment ->
                async {
                    if (comment.avatar.isNotEmpty()) comment.avatar = SupabaseStorageHelper.resolveMediaUrlAsync(comment.avatar)
                    if (comment.image.isNotEmpty()) comment.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(comment.image))
                    if (comment.video.isNotEmpty()) comment.updateVideo(SupabaseStorageHelper.resolveMediaUrlAsync(comment.video))
                    comment
                }
            }.awaitAll()
        }
    }

    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>? {
        val rawList = suspendCancellableCoroutine<List<NotificationDTO>?> { continuation ->
            val result = mutableListOf<NotificationDTO>()
            val databaseReference = FIRDatabase.database().reference()
                .child(DataConstant.USER_PATH)
                .child(currentUserUid)
                .child(path)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    result.clear()
                    if (snapshot != null && snapshot.exists()) {
                        val children = snapshot.children
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue

                            try {
                                val notification = NotificationDTO.fromMap(value as Map<String, Any>)
                                result.add(notification)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (continuation.isActive) continuation.resume(result, onCancellation = {})
                    } else {
                        if (continuation.isActive) continuation.resume(null, onCancellation = {})
                    }
                }
            ) { error ->
                logMessage("getAllNotificationsOfUser", { "Error: ${error?.localizedDescription}" })
                if (continuation.isActive) continuation.resume(null, onCancellation = {})
            }
        } ?: return null

        // Phase 2: resolve notification avatars — NotificationDTO.avatar is val, use copy()
        return coroutineScope {
            rawList.map { notification ->
                async {
                    val resolvedAvatar = if (notification.avatar.isNotEmpty())
                        SupabaseStorageHelper.resolveMediaUrlAsync(notification.avatar)
                    else notification.avatar
                    notification.copy(avatar = resolvedAvatar)
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
        IosDatabaseHelper.saveListToDatabase(id,path,value,externalPath)
    }

    override suspend fun downloadImage(
        image: String,
        fileName: String
    ): Boolean {
        return IosDatabaseHelper.downloadImage(image, fileName)
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean {
        return IosDatabaseHelper.updateNewsFromDatabase(path,newContent,newImage, newVideo,new)
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    ) {
        IosDatabaseHelper.saveNotificationToDatabase(id,path,instance)
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    ) {
        IosDatabaseHelper.deleteNotificationFromDatabase(id, path, notification)
    }

    override suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswerDTO,
        sendOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateDTO,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSessionDTO,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatus
    ): Boolean {
        // iOS implementation will be added later
        return false
    }

    override suspend fun deleteCallSession(
        sessionId: String
    ): Boolean {
        // iOS implementation will be added later
        return false
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateDTO>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateDTO>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswerDTO,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun clearAnswerInFirebase(
        sessionId: String
    ) {
        // iOS implementation will be added later
    }

    override suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String
    ): Boolean? {
        // iOS implementation will be added later
        return null
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (OfferAnswerDTO) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: Utils.Companion.CallStatusCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun cancelObserveAnswerFromCallee(
        sessionId: String,
        callPath: String
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (IceCandidateDTO) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (OfferAnswerDTO) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean {
        // iOS implementation will be added later
        return false
    }

    override fun stopObservePhoneCall() {
        // iOS implementation will be added later
    }

    override fun stopObservePhoneCallWithoutCheckingInCall() {
        // iOS implementation will be added later
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        val rawList = suspendCancellableCoroutine<List<UserDTO>?> { continuation ->
            val database = FIRDatabase.database()
            val databaseReference = database.reference().child(DataConstant.USER_PATH)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val users = mutableListOf<UserDTO>()
                        val children = snapshot.children

                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue

                            try {
                                val user = value.toUserDTO()
                                if (user.name.contains(name, ignoreCase = true)) {
                                    users.add(user)
                                    if (users.size >= 5) break // only return first 5 matches
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }

                        continuation.resume(users) {}
                    } else {
                        continuation.resume(emptyList<UserDTO>()) {}
                    }
                }
            ) { _ -> continuation.resume(null) {} }
        } ?: return null

        return coroutineScope {
            rawList.map { user ->
                async {
                    if (user.image.isNotEmpty()) user.updateImage(SupabaseStorageHelper.resolveMediaUrlAsync(user.image))
                    user
                }
            }.awaitAll()
        }
    }

    // -------------------- Group placeholder implementations (iOS) -------------------- //
    override suspend fun getAllGroups(
        userPath: String,
        groupPath: String,
        userId: String
    ): Set<GroupSummaryDTO> {
        // TODO: Implement iOS get all groups
        return emptySet()
    }

    override suspend fun fetchGroupInfo(groupId: String, groupPath: String): GroupDTO? {
        // TODO: Implement iOS fetch group info
        return null
    }

    override suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        // TODO: Implement iOS notification status update
        return false
    }

    override suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String> {
        // TODO: Implement iOS fetch members
        return HashMap()
    }

    override suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO {
        // TODO: Implement iOS group configs
        return GroupSummaryDTO()
    }

    override suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean {
        // TODO: Implement iOS fetch notification state
        return false
    }

    override suspend fun inviteFriendToGroup(
        friendDto: UserDTO,
        userPath: String,
        notificationPath: String
    ) {
        // TODO: Implement iOS invite friend to group
    }

    override suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean {
        // TODO: Implement iOS add user to group
        return false
    }

    override suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean {
        // TODO: Implement iOS remove user from group
        return false
    }

    override suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean {
        // TODO: Implement iOS delete group
        return false
    }

    override suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean {
        // TODO: Implement iOS update member role
        return false
    }

    override suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> {
        // TODO: Implement iOS fetch recommend/feature groups
        return emptyList()
    }

    override suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getLocalSessionId(): String {
        // TODO: Implement iOS local session ID storage (e.g., NSUserDefaults/Keychain)
        return ""
    }

    override suspend fun updateUserLongField(
        userId: String,
        fieldPath: String,
        value: Long,
        userPath: String
    ): Boolean {
        // TODO: Implement iOS updateUserLongField
        return false
    }
}
