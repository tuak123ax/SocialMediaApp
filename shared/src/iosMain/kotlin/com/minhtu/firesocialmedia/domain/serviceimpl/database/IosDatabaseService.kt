package com.minhtu.firesocialmedia.domain.serviceimpl.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageReference
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.data.model.news.CommentInstance
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.notification.fromMap
import com.minhtu.firesocialmedia.data.model.signin.SignInState
import com.minhtu.firesocialmedia.data.model.user.UserInstance
import com.minhtu.firesocialmedia.data.model.user.toMap
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.platform.toNSData
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toCommentInstance
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toNewsInstance
import com.minhtu.firesocialmedia.utils.IosUtils.Companion.toUserInstance
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDictionary
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class IosDatabaseService() : DatabaseService{
    override suspend fun updateFCMTokenForCurrentUser(currentUser : UserInstance) {
        val currentFCMToken = IosCryptoHelper.getFromKeychain(Constants.KEY_FCM_TOKEN) ?: ""
        if(currentFCMToken.isNotEmpty()) {
            if(currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                IosDatabaseHelper.saveStringToDatabase(currentUser.uid,Constants.USER_PATH, currentFCMToken, Constants.TOKEN_PATH)
            }
        }
    }

    override suspend fun saveValueToDatabase(
        id: String,
        path: String,
        value: HashMap<String, Int>,
        externalPath: String,
        callback : Utils.Companion.BasicCallBack
    ) {
        IosDatabaseHelper.saveValueToDatabase(id,
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
        new: NewsInstance
    ) {
        IosDatabaseHelper.deleteNewsFromDatabase(path, new)
    }

    override suspend fun deleteCommentFromDatabase(
        path: String,
        comment: CommentInstance
    ) {
        IosDatabaseHelper.deleteCommentFromDatabase(path, comment)
    }

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: NewsInstance,
        stateFlow: MutableStateFlow<Boolean?>
    ) {
        IosDatabaseHelper.saveInstanceToDatabase(
            id,
            path,
            instance,
            stateFlow)
    }

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: CommentInstance,
        stateFlow: MutableStateFlow<Boolean?>
    ) {
        IosDatabaseHelper.saveInstanceToDatabase(
            id,
            path,
            instance,
            stateFlow)
    }

    override suspend fun getAllUsers(path: String, callback: Utils.Companion.GetUserCallback) {
        val result = mutableListOf<UserInstance>()
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
                            val user = value.toUserInstance()
                            result.add(user)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    callback.onSuccess(result)
                } else {
                    callback.onFailure()
                }
            }
        )
    }

    override suspend fun getUser(userId: String): UserInstance? {
        return suspendCancellableCoroutine { continuation ->
            val database = FIRDatabase.database()
            val databaseReference = database.reference()
                .child(Constants.USER_PATH)
                .child(userId)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val value = snapshot.value as? Map<*, *> ?: null
                        if (value != null) {
                            try {
                                val user = value.toUserInstance()
                                continuation.resume(user) {}
                            } catch (e: Exception) {
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

    override suspend fun getNew(newId: String): NewsInstance? {
        return suspendCancellableCoroutine { continuation ->
            val database = FIRDatabase.database()
            val databaseReference = database.reference()
                .child(Constants.NEWS_PATH)
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
                                
                                val news = value.toNewsInstance()
                                continuation.resume(news) {}
                            } catch (e: Exception) {
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
        path: String,
        callback: Utils.Companion.GetNewCallback
    ) {
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
                val newsList = mutableListOf<NewsInstance>()
                if (enumerator != null) {
                    while (true) {
                        val child = enumerator.nextObject() as? FIRDataSnapshot ?: break
                        val raw = child.value as? Map<*, *> ?: continue
                        val value = raw.entries
                            .associate { (k, v) -> (k as? String) to v }
                            .filterKeys { it != null } as Map<String, Any?>
                        try {
                            newsList.add(value.toNewsInstance())
                        } catch (_: Exception) {
                        }
                    }
                }

                if (newsList.isNotEmpty()) {
                    newsList.forEach { new ->
                        logMessage("newsList") { "${new.id} message: ${new.message}" }
                    }

                    val sorted = newsList.sortedByDescending { it.timePosted }
                    val oldest = sorted.last()

                    callback.onSuccess(
                        sorted,
                        if (newsList.size < number) null else oldest.timePosted.toDouble(),
                        oldest.id
                    )
                }
            }
        ) { _ ->
            callback.onFailure()
        }
    }

    override suspend fun getAllComments(
        path: String,
        newsId: String,
        callback: Utils.Companion.GetCommentCallback
    ) {
        val result = mutableListOf<CommentInstance>()
        val databaseReference = FIRDatabase
            .database()
            .reference()
            .child(Constants.NEWS_PATH)
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
                            val comment = value.toCommentInstance()
                            logMessage("allComment") { comment.id + ": "+ comment.listReplies.size }
                            result.add(comment)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    callback.onSuccess(ArrayList(result))
                } else {
                    callback.onFailure()
                }
            }
        )
    }


    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String,
        callback: Utils.Companion.GetNotificationCallback
    ) {
        val result = mutableListOf<NotificationInstance>()
        val databaseReference = FIRDatabase.database().reference()
            .child(Constants.USER_PATH)
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
                            val notification = NotificationInstance.fromMap(value as Map<String,Any>)
                            result.add(notification)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    callback.onSuccess(result)
                } else {
                    callback.onFailure()
                }
            }
        ) { error ->
            // Handle error case
            logMessage("getAllNotificationsOfUser", { "Error: ${error?.localizedDescription}" })
            callback.onFailure()
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

    override suspend fun downloadImage(image: String, fileName: String, onComplete: (Boolean) -> Unit) {
        IosDatabaseHelper.downloadImage(image, fileName, onComplete)
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo : String,
        new: NewsInstance,
        status: MutableStateFlow<Boolean?>
    ) {
        IosDatabaseHelper.updateNewsFromDatabase(path,newContent,newImage, newVideo,new,status)
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveSignUpInformation(
        user: UserInstance,
        callBack: Utils.Companion.SaveSignUpInformationCallBack
    ) {
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
            suspendCancellableCoroutine<Unit> { cont ->
                databaseReference.setValue(userMap) { error, _ ->
                    if (error != null) cont.resumeWithException(Throwable(error.localizedDescription))
                    else cont.resume(Unit,
                        onCancellation = {})
                }
            }

            callBack.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            callBack.onFailure()
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationInstance>
    ) {
        IosDatabaseHelper.saveNotificationToDatabase(id,path,instance)
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationInstance
    ) {
        IosDatabaseHelper.deleteNotificationFromDatabase(id, path, notification)
    }

    override suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswer,
        callPath: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateData,
        whichCandidate: String,
        callPath: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSession,
        callPath: String,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatus,
        callPath: String,
        sendCallStatusCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun deleteCallSession(
        sessionId: String,
        callPath: String,
        deleteCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        callPath: String,
        phoneCallCallBack: (String, String, String, OfferAnswer) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        callPath: String,
        phoneCallCallBack: (String, String, String, OfferAnswer) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateData>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswer,
        callPath: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        callPath: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        callPath: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String,
        onResult: (Boolean) -> Unit
    ) {
        // iOS implementation will be added later
        showToast("This feature is not available for iOS now!")
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        callPath: String,
        answerCallBack: (OfferAnswer) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callPath: String,
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
        callPath: String,
        iceCandidateCallBack: (IceCandidateData) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        callPath: String,
        videoCallCallBack: (OfferAnswer) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun searchUserByName(
        name: String,
        path: String
    ): List<UserInstance>? {
        return suspendCancellableCoroutine { continuation ->
            val database = FIRDatabase.database()
            val databaseReference = database.reference().child(Constants.USER_PATH)

            databaseReference.observeSingleEventOfType(
                FIRDataEventType.FIRDataEventTypeValue,
                withBlock = { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        val users = mutableListOf<UserInstance>()
                        val children = snapshot.children
                        
                        while (true) {
                            val child = children.nextObject() as? FIRDataSnapshot ?: break
                            val value = child.value as? Map<*, *> ?: continue
                            
                            try {
                                val user = value.toUserInstance()
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
                        continuation.resume(emptyList<UserInstance>()) {}
                    }
                }
            ) { error ->
                continuation.resume(null) {}
            }
        }
    }

    override fun checkUserExists(email: String, callback: (SignInState) -> Unit) {
        val database = FIRDatabase.database()
        val ref = database.reference().child("users")

        ref.observeEventType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot: FIRDataSnapshot? ->
                if (snapshot == null || !snapshot.exists()) {
                    callback(SignInState(true, Constants.ACCOUNT_NOT_EXISTED))
                    return@observeEventType
                }

                val children = snapshot.children

                var existed = false

                while (true) {
                    val child = children.nextObject() as? FIRDataSnapshot ?: break
                    val value = child.value as? Map<*,*> ?: continue
                    try {
                        val user = value.toUserInstance()
                        if (user.email == email) {
                            callback.invoke(SignInState(true, Constants.ACCOUNT_EXISTED))
                            existed = true
                            break
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }

                if (!existed) {
                    callback.invoke(SignInState(true, Constants.ACCOUNT_NOT_EXISTED))
                }
            },
            withCancelBlock = { error ->
                callback.invoke(SignInState(false, Constants.LOGIN_ERROR))
            }
        )
    }
}
