package com.minhtu.firesocialmedia.domain.serviceimpl.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageReference
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.constant.DataConstant
import com.minhtu.firesocialmedia.data.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.dto.comment.CommentDTO
import com.minhtu.firesocialmedia.data.dto.home.LatestNewsDTO
import com.minhtu.firesocialmedia.data.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.dto.notification.fromMap
import com.minhtu.firesocialmedia.data.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.dto.user.toMap
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.toNSData
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toCommentDTO
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toNewsDTO
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toUserDTO
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDictionary
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

    override suspend fun getAllUsers(path: String): ArrayList<UserDTO>? = suspendCancellableCoroutine{ continuation ->
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
                    if(continuation.isActive) continuation.resume(result, onCancellation = {})
                } else {
                    if(continuation.isActive) continuation.resume(null, onCancellation = {})
                }
            }
        )
    }

    override suspend fun getUser(userId: String): UserDTO? {
        return suspendCancellableCoroutine { continuation ->
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
            ) { error ->
                continuation.resume(null) {}
            }
        }
    }

    override suspend fun getNew(newId: String): NewsDTO? {
        return suspendCancellableCoroutine { continuation ->
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
            ) { error ->
                continuation.resume(null) {}
            }
        }
    }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ): LatestNewsDTO = suspendCancellableCoroutine{ continuation ->
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

                    if(continuation.isActive) continuation.resume(LatestNewsDTO(
                        sorted,
                        if (newsList.size < number) null else oldest.timePosted.toDouble(),
                        oldest.id
                    ), onCancellation = {})
                }
            }
        ) { _ ->
            continuation.resume(LatestNewsDTO(null, null, null), onCancellation = {})
        }
    }

    override suspend fun getAllComments(
        path: String,
        newsId: String
    ): List<CommentDTO>? = suspendCancellableCoroutine { continuation ->
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
                            logMessage("allComment") { comment.id + ": "+ comment.listReplies.size }
                            result.add(comment)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if(continuation.isActive) continuation.resume(ArrayList(result), onCancellation = {})
                } else {
                    if(continuation.isActive) continuation.resume(null, onCancellation = {})
                }
            }
        )
    }

    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationDTO>? = suspendCancellableCoroutine { continuation ->
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
                            val notification = NotificationDTO.fromMap(value as Map<String,Any>)
                            result.add(notification)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if(continuation.isActive) continuation.resume(result, onCancellation = {})
                } else {
                    if(continuation.isActive) continuation.resume(null, onCancellation = {})
                }
            }
        ) { error ->
            // Handle error case
            logMessage("getAllNotificationsOfUser", { "Error: ${error?.localizedDescription}" })
            if(continuation.isActive) continuation.resume(null, onCancellation = {})
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

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        val storageReference: FIRStorageReference = FIRStorage.storage().reference().child("avatar").child(user.uid)
        val databaseReference: FIRDatabaseReference = FIRDatabase.database().reference().child("users").child(user.uid)
        try {
            if (user.image != Constants.DEFAULT_AVATAR_URL) {
                val nsDataAvatar = Base64.decode(user.image).toNSData()
                val metadata = FIRStorageMetadata().apply {
                    setContentType("image/jpeg")
                }

                try{
                    val avatarRemoteUrl = IosDatabaseHelper.uploadAndGetRemoteURL(storageReference,nsDataAvatar,metadata)
                    user.updateImage(avatarRemoteUrl)
                } catch(e : Exception) {
                    logMessage("saveSignUpInformation") { e.message.toString() }
                }
            }

            // Convert user to Firebase-compatible Map
            val userMap = user.toMap() as NSDictionary

            // Save user object in Realtime Database
            databaseReference.setValue(userMap)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
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

    override fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatus,
        sendCallStatusCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun deleteCallSession(
        sessionId: String,
        deleteCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateDTO>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
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

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserDTO>? {
        return suspendCancellableCoroutine { continuation ->
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
            ) { error ->
                continuation.resume(null) {}
            }
        }
    }
}
