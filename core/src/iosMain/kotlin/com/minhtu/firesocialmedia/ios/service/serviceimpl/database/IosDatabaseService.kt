package com.minhtu.firesocialmedia.ios.service.serviceimpl.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
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
import com.minhtu.firesocialmedia.data.remote.dto.notification.fromMap
import com.minhtu.firesocialmedia.data.remote.dto.notification.toMap
import com.minhtu.firesocialmedia.data.remote.dto.settings.PollDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.SessionItemDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.security.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toCommentDTO
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toNewsDTO
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toUserDTO
import com.minhtu.firesocialmedia.core.utils.Utils
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice
private fun <T> CancellableContinuation<T>.resume(value: T) {
    this.resume(value, onCancellation = null)
}

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
                    if(continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED))
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
                            if(continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_EXISTED))
                            existed = true
                            break
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }

                if (!existed) {
                    if(continuation.isActive) continuation.resume(SignInDTO(true, Constants.ACCOUNT_NOT_EXISTED))
                }
            },
            withCancelBlock = { error ->
                if(continuation.isActive) continuation.resume(SignInDTO(false, Constants.LOGIN_ERROR))
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
        SupabaseStorageHelper().deleteNewsFromDatabase(path, new)
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
        return SupabaseStorageHelper().saveInstanceToDatabase(id, path, instance)
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
                        if (continuation.isActive) continuation.resume(result)
                    } else {
                        if (continuation.isActive) continuation.resume(null)
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
                        ))
                    } else {
                        if (continuation.isActive) continuation.resume(
                            LatestNewsDTO(emptyList(), null, null)
                        )
                    }
                }
            ) { _ ->
                continuation.resume(LatestNewsDTO(null, null, null))
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
                        if (continuation.isActive) continuation.resume(ArrayList(result))
                    } else {
                        if (continuation.isActive) continuation.resume(null)
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
                        if (continuation.isActive) continuation.resume(result)
                    } else {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            ) { error ->
                logMessage("getAllNotificationsOfUser", { "Error: ${error?.localizedDescription}" })
                if (continuation.isActive) continuation.resume(null)
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
        return SupabaseStorageHelper().updateNewsFromDatabase(path, newContent, newImage, newVideo, new)
    }

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return SupabaseStorageHelper().saveNewToDatabase(commentId, path, instance)
    }

    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        return SupabaseStorageHelper().saveSignUpInformation(user)
    }

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        return SupabaseStorageHelper().saveGroupAndUserGroups(
            groupRootPath, userRootPath, userGroupsField, groupAvatarsStoragePath, group, userId
        )
    }

    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        return SupabaseStorageHelper().saveNewToGroup(newsDTO, groupId, groupPath, postsPath, imagePath)
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
    ): Set<GroupSummaryDTO> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath)
            .child(userId)
            .child(groupPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = mutableSetOf<GroupSummaryDTO>()
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val map = child.value as? Map<*, *> ?: continue
                    val id = map["id"] as? String ?: child.key ?: continue
                    val name = map["name"] as? String ?: ""
                    val avatar = map["avatar"] as? String ?: ""
                    val notificationOn = map["notificationOn"] as? Boolean ?: false
                    result.add(GroupSummaryDTO(id = id, name = name, avatar = avatar, notificationOn = notificationOn))
                }
            }
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptySet()) }
    }

    override suspend fun fetchGroupInfo(groupId: String, groupPath: String): GroupDTO? = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(groupPath).child(groupId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.value as? Map<*, *>
                if (map != null) {
                    val group = GroupDTO(
                        id = map["id"] as? String ?: groupId,
                        name = map["name"] as? String ?: "",
                        avatar = map["avatar"] as? String ?: "",
                        password = map["password"] as? String ?: "",
                        description = map["description"] as? String ?: "",
                        createdDate = (map["createdDate"] as? Long) ?: 0L,
                        memberCount = (map["memberCount"] as? Long) ?: 0L
                    )
                    if (cont.isActive) cont.resume(group)
                } else {
                    if (cont.isActive) cont.resume(null)
                }
            } else {
                if (cont.isActive) cont.resume(null)
            }
        }) { _ -> if (cont.isActive) cont.resume(null) }
    }

    override suspend fun updateNotificationStatus(
        newStatus: Boolean,
        groupId: String,
        userId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(groupPath).child(groupId).child(notificationStatusPath)
        ref.setValue(newStatus) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun getAllMembersInGroup(
        groupId: String,
        groupPath: String,
        membersPath: String
    ): HashMap<String, String> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(groupPath).child(groupId).child(membersPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = HashMap<String, String>()
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val key = child.key ?: continue
                    val value = child.value as? String ?: continue
                    result[key] = value
                }
            }
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(HashMap()) }
    }

    override suspend fun getGroupConfigs(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String
    ): GroupSummaryDTO = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(groupPath).child(groupId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.value as? Map<*, *>
                val summary = if (map != null) GroupSummaryDTO(
                    id = map["id"] as? String ?: groupId,
                    name = map["name"] as? String ?: "",
                    avatar = map["avatar"] as? String ?: "",
                    notificationOn = map["notificationOn"] as? Boolean ?: false
                ) else GroupSummaryDTO()
                if (cont.isActive) cont.resume(summary)
            } else {
                if (cont.isActive) cont.resume(GroupSummaryDTO())
            }
        }) { _ -> if (cont.isActive) cont.resume(GroupSummaryDTO()) }
    }

    override suspend fun fetchNotificationState(
        userId: String,
        groupId: String,
        userPath: String,
        groupPath: String,
        notificationStatusPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(groupPath).child(groupId).child(notificationStatusPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val value = snapshot?.value as? Boolean ?: false
            if (cont.isActive) cont.resume(value)
        }) { _ -> if (cont.isActive) cont.resume(false) }
    }

    override suspend fun inviteFriendToGroup(
        friendDto: UserDTO,
        userPath: String,
        notificationPath: String
    ) {
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(friendDto.uid).child(notificationPath)
        val notifList = friendDto.notifications.map { it.toMap() }
        ref.setValue(notifList) { _, _ -> }
    }

    override suspend fun addUserToGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val groupSummaryMap = mapOf<Any?, Any?>(
            "id" to group.id, "name" to group.name, "avatar" to group.avatar
        )
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/${group.id}/$memberPath/${user.uid}" to "member",
            "$userPath/${user.uid}/$groupPath/${group.id}" to groupSummaryMap
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (error == null) {
                // Increment memberCount
                val countRef = dbRef.child(groupPath).child(group.id).child(memberCountPath)
                countRef.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                    val current = (snapshot?.value as? Long) ?: 0L
                    countRef.setValue(current + 1) { _, _ -> }
                }) { _ -> }
            }
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun removeUserFromGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath: String,
        memberCountPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/${group.id}/$memberPath/${user.uid}" to null,
            "$userPath/${user.uid}/$groupPath/${group.id}" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (error == null) {
                val countRef = dbRef.child(groupPath).child(group.id).child(memberCountPath)
                countRef.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                    val current = (snapshot?.value as? Long) ?: 0L
                    val newCount = if (current > 0) current - 1 else 0
                    countRef.setValue(newCount) { _, _ -> }
                }) { _ -> }
            }
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun deleteGroup(
        user: UserDTO,
        group: GroupDTO,
        userPath: String,
        groupPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/${group.id}" to null,
            "$userPath/${user.uid}/$groupPath/${group.id}" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun updateMemberRole(
        role: String,
        user: UserDTO,
        group: GroupDTO,
        groupPath: String,
        memberPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(groupPath).child(group.id).child(memberPath).child(user.uid)
        ref.setValue(role) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun fetchRecommendGroups(
        limit: Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(groupPath)
        ref.queryOrderedByChild(memberCountPath).queryLimitedToLast(limit.toULong())
            .observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
                val result = mutableListOf<GroupDTO>()
                if (snapshot != null && snapshot.exists()) {
                    val children = snapshot.children
                    while (true) {
                        val child = children.nextObject() as? FIRDataSnapshot ?: break
                        val map = child.value as? Map<*, *> ?: continue
                        result.add(GroupDTO(
                            id = map["id"] as? String ?: child.key ?: "",
                            name = map["name"] as? String ?: "",
                            avatar = map["avatar"] as? String ?: "",
                            memberCount = (map["memberCount"] as? Long) ?: 0L
                        ))
                    }
                }
                if (cont.isActive) cont.resume(result.sortedByDescending { it.memberCount })
            }) { _ -> if (cont.isActive) cont.resume(emptyList()) }
    }

    override suspend fun updateIsReadStatusOfNotification(
        userId: String,
        notificationId: String,
        userPath: String,
        notificationPath: String
    ) {
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(notificationPath)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val id = (child.value as? Map<*, *>)?.get("id") as? String ?: continue
                    if (id == notificationId) {
                        child.ref.child("beRead").setValue(true) { _, _ -> }
                        break
                    }
                }
            }
        }) { _ -> }
    }

    override suspend fun deleteAllNotifications(
        uid: String,
        userPath: String,
        notificationPath: String
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(uid).child(notificationPath)
        ref.removeValueWithCompletionBlock { error, _ ->
            if (cont.isActive) {
                if (error == null) cont.resume(Result.success(Unit))
                else cont.resume(Result.failure(Exception(error.localizedDescription)))
            }
        }
    }

    override fun getLocalSessionId(): String {
        return IosCryptoHelper.getFromKeychain(Constants.KEY_SESSION_ID) ?: ""
    }

    override fun clearLocalSessionId() {
        IosCryptoHelper.saveToKeychain(Constants.KEY_SESSION_ID, "")
    }

    override suspend fun deleteLoginSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId)
        ref.removeValueWithCompletionBlock { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun logoutSession(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId).child("status")
        ref.setValue("LOGOUT") { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    private var sessionStatusHandle: ULong? = null
    private var sessionStatusRef: FIRDatabaseReference? = null

    override fun observeSessionStatus(
        userId: String,
        sessionId: String,
        historyPath: String,
        loginHistoryPath: String,
        onLoggedOut: () -> Unit
    ) {
        stopObserveSessionStatus()
        if (sessionId.isEmpty()) return
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId).child("status")
        sessionStatusRef = ref
        sessionStatusHandle = ref.observeEventType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot ->
                val status = snapshot?.value as? String ?: return@observeEventType
                if (status == "LOGOUT") onLoggedOut()
            }
        )
    }

    override fun stopObserveSessionStatus() {
        val handle = sessionStatusHandle
        val ref = sessionStatusRef
        if (handle != null && ref != null) {
            ref.removeObserverWithHandle(handle)
        }
        sessionStatusRef = null
        sessionStatusHandle = null
    }

    override suspend fun updateUserLongField(
        userId: String,
        fieldPath: String,
        value: Long,
        userPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(fieldPath)
        ref.setValue(value) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun updateUserStringField(
        userId: String,
        fieldPath: String,
        value: String,
        userPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(fieldPath)
        ref.setValue(value) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean {
        return try {
            val remotePath = "avatar/${userId}_${getCurrentTime()}.jpg"
            SupabaseStorage.uploadFile(imageUri, remotePath)
            updateUserStringField(userId, "image", remotePath, userPath)
        } catch (e: Exception) {
            logMessage("updateUserAvatar", { "Failed: ${e.message}" })
            false
        }
    }

    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean {
        return try {
            val remotePath = "background/${userId}_${getCurrentTime()}.jpg"
            SupabaseStorage.uploadFile(imageUri, remotePath)
            updateUserStringField(userId, "background", remotePath, userPath)
        } catch (e: Exception) {
            logMessage("updateUserBackground", { "Failed: ${e.message}" })
            false
        }
    }

    override suspend fun updateTwoFAEnabledFlagForUser(
        userId: String,
        twoFAEnabled: Boolean,
        userPath: String,
        twoFaEnabledPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(userPath).child(userId).child(twoFaEnabledPath)
        ref.setValue(twoFAEnabled) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun fetchLoginHistoryList(
        userId: String,
        historyPath: String,
        loginHistoryPath: String
    ): List<SessionItemDTO> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(historyPath).child(loginHistoryPath).child(userId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = mutableListOf<SessionItemDTO>()
            if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val map = child.value as? Map<*, *> ?: continue
                    val sessionId = child.key ?: ""
                    result.add(SessionItemDTO(
                        sessionId = map["sessionId"] as? String ?: sessionId,
                        deviceName = map["deviceName"] as? String ?: "",
                        location = map["location"] as? String ?: "",
                        time = (map["time"] as? Long) ?: 0L,
                        status = map["status"] as? String ?: ""
                    ))
                }
            }
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptyList()) }
    }

    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        try {
            val sessionId = NSUUID.UUID().UUIDString()
            IosCryptoHelper.saveToKeychain(Constants.KEY_SESSION_ID, sessionId)
            val deviceName = UIDevice.currentDevice.name
            val location = locationInfo.locationInfo()
            val timeMillis = getCurrentTime()
            val sessionMap = mapOf<Any?, Any?>(
                "sessionId" to sessionId,
                "deviceName" to deviceName,
                "location" to location,
                "time" to timeMillis,
                "status" to "ACTIVE"
            )
            val ref = FIRDatabase.database().reference()
                .child(historyPath).child(loginHistoryPath).child(userId).child(sessionId)
            suspendCancellableCoroutine<Unit> { cont ->
                ref.setValue(sessionMap) { _, _ -> if (cont.isActive) cont.resume(Unit) }
            }
        } catch (e: Exception) {
            logMessage("saveLoginActivityInfo", { "Exception: ${e.message}" })
        }
    }

    override suspend fun createPoll(
        poll: PollDTO,
        pollPath: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        newsEntry: NewsDTO
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val pollMap = mapOf<Any?, Any?>(
            "id" to poll.id,
            "posterId" to poll.posterId,
            "posterName" to poll.posterName,
            "posterAvatar" to poll.posterAvatar,
            "question" to poll.question,
            "options" to poll.options,
            "allowMultipleAnswers" to poll.allowMultipleAnswers,
            "duration" to poll.duration,
            "groupId" to poll.groupId,
            "likeCount" to poll.likeCount,
            "commentCount" to poll.commentCount,
            "timePosted" to poll.timePosted,
            "expiresAt" to poll.expiresAt,
            "votes" to (poll.votes ?: emptyMap<String, Int>())
        )
        val newsMap = mapOf<Any?, Any?>(
            "id" to newsEntry.id,
            "posterId" to newsEntry.posterId,
            "posterName" to newsEntry.posterName,
            "avatar" to newsEntry.avatar,
            "message" to newsEntry.message,
            "timePosted" to newsEntry.timePosted
        )
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/$groupId/$postsPath/${newsEntry.id}" to newsMap,
            "$pollPath/${poll.id}" to pollMap
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun deletePollFromDatabase(
        newsId: String,
        pollId: String,
        groupPath: String,
        groupId: String,
        postsPath: String,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$groupPath/$groupId/$postsPath/$newsId" to null,
            "$pollPath/$pollId" to null,
            "$pollVotesPath/$pollId" to null
        )
        dbRef.updateChildValues(updates) { error, _ ->
            if (cont.isActive) cont.resume(error == null)
        }
    }

    override suspend fun fetchPoll(pollId: String, pollPath: String): PollDTO? = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(pollPath).child(pollId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val map = snapshot.value as? Map<*, *>
                if (map != null) {
                    val optionsRaw = map["options"]
                    val options: List<String> = when (optionsRaw) {
                        is List<*> -> optionsRaw.mapNotNull { it as? String }
                        is Map<*, *> -> optionsRaw.values.mapNotNull { it as? String }
                        else -> emptyList()
                    }
                    val votesRaw = map["votes"] as? Map<*, *>
                    val votes: Map<String, Int>? = votesRaw?.mapNotNull { (k, v) ->
                        val key = k as? String ?: return@mapNotNull null
                        val value = (v as? Long)?.toInt() ?: (v as? Int) ?: return@mapNotNull null
                        key to value
                    }?.toMap()
                    val poll = PollDTO(
                        id = map["id"] as? String ?: pollId,
                        posterId = map["posterId"] as? String ?: "",
                        posterName = map["posterName"] as? String ?: "",
                        posterAvatar = map["posterAvatar"] as? String ?: "",
                        question = map["question"] as? String ?: "",
                        options = options,
                        allowMultipleAnswers = map["allowMultipleAnswers"] as? Boolean ?: false,
                        duration = map["duration"] as? String ?: "",
                        groupId = map["groupId"] as? String ?: "",
                        likeCount = (map["likeCount"] as? Long)?.toInt() ?: 0,
                        commentCount = (map["commentCount"] as? Long)?.toInt() ?: 0,
                        timePosted = (map["timePosted"] as? Long) ?: 0L,
                        expiresAt = map["expiresAt"] as? Long,
                        votes = votes
                    )
                    if (cont.isActive) cont.resume(poll)
                } else {
                    if (cont.isActive) cont.resume(null)
                }
            } else {
                if (cont.isActive) cont.resume(null)
            }
        }) { _ -> if (cont.isActive) cont.resume(null) }
    }

    override suspend fun loadMyVotes(
        pollId: String,
        userId: String,
        pollVotesPath: String
    ): List<Int> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference()
            .child(pollVotesPath).child(pollId).child(userId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = if (snapshot != null && snapshot.exists()) {
                val children = snapshot.children
                val list = mutableListOf<Int>()
                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    (child.value as? Long)?.toInt()?.let { list.add(it) }
                }
                list
            } else emptyList()
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptyList()) }
    }

    override suspend fun loadAllVoters(pollId: String, pollVotesPath: String): Map<String, List<Int>> = suspendCancellableCoroutine { cont ->
        val ref = FIRDatabase.database().reference().child(pollVotesPath).child(pollId)
        ref.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val result = mutableMapOf<String, List<Int>>()
            if (snapshot != null && snapshot.exists()) {
                val users = snapshot.children
                while (true) {
                    val userSnapshot = users.nextObject() as? FIRDataSnapshot ?: break
                    val uid = userSnapshot.key ?: continue
                    val indices = mutableListOf<Int>()
                    val voteChildren = userSnapshot.children
                    while (true) {
                        val child = voteChildren.nextObject() as? FIRDataSnapshot ?: break
                        (child.value as? Long)?.toInt()?.let { indices.add(it) }
                    }
                    result[uid] = indices
                }
            }
            if (cont.isActive) cont.resume(result)
        }) { _ -> if (cont.isActive) cont.resume(emptyMap()) }
    }

    override suspend fun submitVote(
        pollId: String,
        userId: String,
        selectedIndices: List<Int>,
        previousIndices: List<Int>,
        pollPath: String,
        pollVotesPath: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dbRef = FIRDatabase.database().reference()
        val updates = hashMapOf<Any?, Any?>(
            "$pollVotesPath/$pollId/$userId" to selectedIndices
        )
        // We need to update vote counts by reading current values first
        val pollRef = dbRef.child(pollPath).child(pollId).child("votes")
        pollRef.observeSingleEventOfType(FIRDataEventType.FIRDataEventTypeValue, withBlock = { snapshot ->
            val currentVotes = (snapshot?.value as? Map<*, *>)?.mapNotNull { (k, v) ->
                val key = k as? String ?: return@mapNotNull null
                val value = (v as? Long)?.toInt() ?: return@mapNotNull null
                key to value
            }?.toMap()?.toMutableMap() ?: mutableMapOf()

            previousIndices.forEach { idx ->
                if (!selectedIndices.contains(idx)) {
                    val current = currentVotes[idx.toString()] ?: 0
                    currentVotes[idx.toString()] = maxOf(0, current - 1)
                }
            }
            selectedIndices.forEach { idx ->
                if (!previousIndices.contains(idx)) {
                    val current = currentVotes[idx.toString()] ?: 0
                    currentVotes[idx.toString()] = current + 1
                }
            }
            currentVotes.forEach { (k, v) -> updates["$pollPath/$pollId/votes/$k"] = v }
            dbRef.updateChildValues(updates) { error, _ ->
                if (cont.isActive) cont.resume(error == null)
            }
        }) { _ -> if (cont.isActive) cont.resume(false) }
    }
}
