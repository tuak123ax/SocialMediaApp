package com.minhtu.firesocialmedia.domain.serviceimpl.database

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
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
import com.minhtu.firesocialmedia.data.remote.dto.signin.SignInDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.error.signin.SignInError
import com.minhtu.firesocialmedia.domain.serviceimpl.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidDatabaseService(context: Context) : DatabaseService {
    // Never hold a Service context to avoid leaks; keep only applicationContext
    private val appContext: Context = context.applicationContext
    override suspend fun updateFCMTokenForCurrentUser(currentUser: UserDTO) {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(appContext)
        val currentFCMToken = secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")
        if(!currentFCMToken.isNullOrEmpty()) {
            if(currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                AndroidDatabaseHelper.saveStringToDatabase(currentUser.uid, DataConstant.USER_PATH, currentFCMToken, DataConstant.TOKEN_PATH)
            }
        }
    }

    override suspend fun checkUserExists(email: String) : SignInDTO = suspendCancellableCoroutine{ continuation ->
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
        externalPath: String) : Boolean {
        return AndroidDatabaseHelper.saveValueToDatabase(id,
            path,
            value,
            externalPath)
    }

    override suspend fun updateCountValueInDatabase(
        id: String,
        path: String,
        externalPath: String,
        value: Int
    ) {
        AndroidDatabaseHelper.updateCountValueInDatabase(id,
            path,
            externalPath,
            value)
    }

    override suspend fun deleteNewsFromDatabase(
        path: String,
        new: NewsDTO
    ) {
        AndroidDatabaseHelper.deleteNewsFromDatabase(path, new)
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
    ) : Boolean{
        return AndroidDatabaseHelper.saveInstanceToDatabase(
            commentId,
            path,
            instance)
    }

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return AndroidDatabaseHelper.saveNewToDatabase(
            commentId,
            path,
            instance)
    }

    override suspend fun getAllUsers(path: String) : ArrayList<UserDTO>? = suspendCancellableCoroutine{ continuation ->
        val result = ArrayList<UserDTO>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference().child(DataConstant.USER_PATH)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val user: UserDTO? = dataSnapshot.getValue(UserDTO::class.java)
                    if (user != null) {
                        result.add(user)
                    }
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
    }

    override suspend fun getUser(userId: String): UserDTO? =
        withTimeout(5000) {
            suspendCoroutine { continuation ->
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference()
                    .child(DataConstant.USER_PATH)
                    .child(userId)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(UserDTO::class.java)
                        continuation.resume(user)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        }

    override suspend fun getNew(newId: String): NewsDTO? = withTimeout(5000) {
        suspendCoroutine { continuation ->
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference()
                .child(DataConstant.NEWS_PATH)
                .child(newId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val new = snapshot.getValue(NewsDTO::class.java)
                    if (new != null) {
                        continuation.resume(new)
                    } else {
                        continuation.resume(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(null)
                }
            })
        }
    }

    override suspend fun searchUserByName(name: String, path: String) : List<UserDTO>? =
        withTimeout(5000) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference(path)

            suspendCoroutine { continuation ->
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.children
                            .mapNotNull { it.getValue(UserDTO::class.java) }
                            .filter { it.name.contains(name, ignoreCase = true) }
                            .take(5) // only return first 5 matches

                        continuation.resume(users)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        }

    override suspend fun getLatestNews(
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        path: String
    ) : LatestNewsDTO = suspendCancellableCoroutine{ continuation ->
        val dbRef = FirebaseDatabase.getInstance()
            .getReference(path)
            .orderByChild("timePosted")
            .let { query ->
                when {
                    lastTimePosted != null && !lastKey.isNullOrBlank() -> {
                        // Use both value and key for stable pagination when key is valid
                        query.endBefore(lastTimePosted, lastKey)
                    }
                    lastTimePosted != null -> {
                        // Fallback: paginate by value only when key is null/blank
                        query.endBefore(lastTimePosted)
                    }
                    else -> query
                }
            }
            .limitToLast(number)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newsList = snapshot.children.mapNotNull { it.getValue(NewsDTO::class.java) }

                if (newsList.isNotEmpty()) {
                    // Sort newest → oldest
                    val sorted = newsList.sortedByDescending { it.timePosted }.map { it }
                    val oldest = sorted.last()
                    if (continuation.isActive) {
                        dbRef.removeEventListener(this)
                        continuation.resume(LatestNewsDTO(
                            sorted,
                            if (newsList.size < number) null else oldest.timePosted.toDouble(),
                            oldest.id // Return both for next pagination
                        ))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) {
                    dbRef.removeEventListener(this)
                    continuation.resume(LatestNewsDTO())
                }
            }
        }
        dbRef.addValueEventListener(listener)
        continuation.invokeOnCancellation { dbRef.removeEventListener(listener) }
    }


    override suspend fun getAllComments(
        path: String,
        newsId : String) : List<CommentDTO>? = suspendCancellableCoroutine { continuation ->
        val result = ArrayList<CommentDTO>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference()
            .child(DataConstant.NEWS_PATH)
            .child(newsId)
            .child(path)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val comments: CommentDTO? = dataSnapshot.getValue(CommentDTO::class.java)
                    if (comments != null) {
                        result.add(comments)
                    }
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
    }

    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid : String
    ) : List<NotificationDTO>? = suspendCancellableCoroutine { continuation ->
        val result = ArrayList<NotificationDTO>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference().child(DataConstant.USER_PATH)
            .child(currentUserUid).child(path)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val notification = dataSnapshot.getValue(NotificationDTO::class.java)
                    if (notification != null) {
                        logMessage("getAllNotificationsOfUser",
                            { notification.id + "isRead: "+ notification.beRead })
                        result.add(notification)
                    }
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
    }

    override suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    ) {
        AndroidDatabaseHelper.saveListToDatabase(id,path,value,externalPath)
    }

    override suspend fun downloadImage(image: String, fileName: String) : Boolean {
        return AndroidDatabaseHelper.downloadImage(appContext, image, fileName)
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean {
        return AndroidDatabaseHelper.updateNewsFromDatabase(path,newContent,newImage,newVideo,new)
    }

    override suspend fun saveSignUpInformation(user : UserDTO) : Boolean = suspendCancellableCoroutine{ continuation ->
        val storageReference = FirebaseStorage.getInstance().getReference()
            .child("avatar").child(user.uid)
        val databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(user.uid)

        if(user.image != Constants.DEFAULT_AVATAR_URL && user.image != Constants.DEFAULT_DECADE_AVATAR_URL && user.image != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP){
            val metadata = StorageMetadata.Builder()
                .setCacheControl("public,max-age=604800,immutable")
                .build()
            storageReference.putFile(user.image.toUri(), metadata).addOnCompleteListener{ putFileTask ->
                if(putFileTask.isSuccessful){
                    storageReference.downloadUrl.addOnSuccessListener { avatarUrl ->
                        user.updateImage(avatarUrl.toString())
                        databaseReference.setValue(user).addOnCompleteListener{addUserTask ->
                            if(addUserTask.isSuccessful){
                                if(continuation.isActive) continuation.resume(true)
                            } else {
                                if(continuation.isActive) continuation.resume(false)
                            }
                        }
                    }
                }
            }
        } else {
            databaseReference.setValue(user).addOnCompleteListener{addUserTask ->
                if(addUserTask.isSuccessful){
                    if(continuation.isActive) continuation.resume(true)
                } else {
                    if(continuation.isActive) continuation.resume(false)
                }
            }
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationDTO>
    ) {
        AndroidDatabaseHelper.saveNotificationToDatabase(id,path,instance)
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationDTO
    ) {
        AndroidDatabaseHelper.deleteNotificationFromDatabase(id, path, notification)
    }

    override suspend fun sendOfferToFireBase(
        sessionId : String,
        offer : OfferAnswerDTO,
        sendOfferCallBack : Utils.Companion.BasicCallBack) {
        AndroidDatabaseHelper.sendOfferToFireBase(
            sessionId,
            offer,
            DataConstant.CALL_PATH,
            sendOfferCallBack
        )
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId : String,
        iceCandidate: IceCandidateDTO,
        whichCandidate : String,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack) {
        AndroidDatabaseHelper.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate,
            whichCandidate,
            DataConstant.CALL_PATH,
            sendIceCandidateCallBack)
    }

    override suspend fun sendCallSessionToFirebase(session: AudioCallSessionDTO,
                                                   sendCallSessionCallBack : Utils.Companion.BasicCallBack) {
        AndroidDatabaseHelper.sendCallSessionToFirebase(session, DataConstant.CALL_PATH, sendCallSessionCallBack)
    }

    override suspend fun sendCallStatusToFirebase(
                                    sessionId : String,
                                    status: CallStatus) : Boolean{
        return AndroidDatabaseHelper.sendCallStatusToFirebase(sessionId, status, DataConstant.CALL_PATH)
    }

    override suspend fun sendWhoEndCall(
        sessionId: String,
        whoEndCall: String
    ): Boolean {
        return AndroidDatabaseHelper.sendWhoEndCall(sessionId, whoEndCall, DataConstant.CALL_PATH)
    }

    override suspend fun deleteCallSession(sessionId: String) : Boolean{
        return AndroidDatabaseHelper.deleteCallSession(sessionId, DataConstant.CALL_PATH)
    }

    override suspend fun observePhoneCall(
        isInCall : MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack : (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateDTO>?) -> Unit) {
        AndroidDatabaseHelper.observePhoneCall(
            isInCall,
            currentUserId,
            DataConstant.CALL_PATH,
            phoneCallCallBack,
            endCallSession,
            whoEndCallCallBack,
            iceCandidateCallBack)
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
        groupAvatarsStoragePath : String,
        group: GroupDTO,
        userId: String): Boolean {
        return AndroidDatabaseHelper.saveGroupAndUserGroups(
            groupRootPath,
            userRootPath,
            userGroupsField,
            groupAvatarsStoragePath,
            group,
            userId
        )
    }

    override suspend fun getAllGroups(
        userPath : String,
        groupPath : String,
        userId: String): Set<GroupSummaryDTO> {
        return AndroidDatabaseHelper.getAllGroups(userPath, groupPath,userId)
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
        imagePath : String
    ): Boolean {
        return AndroidDatabaseHelper.saveNewToGroup(
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
        userId : String,
        userPath : String,
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
        notificationStatusPath : String
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
        userPath : String,
        notificationPath : String) {
        return AndroidDatabaseHelper.inviteFriendToGroup(
            friendDto,
            userPath,
            notificationPath
        )
    }

    override suspend fun addUserToGroup(
        user: UserDTO,
        group : GroupDTO,
        userPath: String,
        groupPath: String,
        memberPath : String,
        memberCountPath : String
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
        memberCountPath : String
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
        role : String,
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
        limit : Int,
        groupPath: String,
        memberCountPath: String
    ): List<GroupDTO> {
        return AndroidDatabaseHelper.fetchGroupsByMemberCount(
            limit,
            groupPath,
            memberCountPath
        )
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
        phoneCallCallBack : (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack : (String) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateDTO>?) -> Unit) {
        AndroidDatabaseHelper.observePhoneCallWithoutCheckingInCall(
            currentUserId,
            DataConstant.CALL_PATH,
            phoneCallCallBack,
            endCallSession,
            whoEndCallCallBack,
            iceCandidateCallBack)
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
    ) : Boolean? = suspendCancellableCoroutine { continuation ->
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
            if(continuation.isActive) continuation.resume(!isBusy)
        }.addOnFailureListener {
            if(continuation.isActive) continuation.resume(false)
        }
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (answer : OfferAnswerDTO) -> Unit,
        rejectCallBack : () -> Unit
    ) {
        AndroidDatabaseHelper.observeAnswerFromCallee(
            sessionId,
            DataConstant.CALL_PATH,
            answerCallBack,
            rejectCallBack)
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: Utils.Companion.CallStatusCallBack
    ) {
        AndroidDatabaseHelper.observeCallStatus(
            sessionId,
            DataConstant.CALL_PATH,
            callStatusCallBack)
    }

    override suspend fun cancelObserveAnswerFromCallee(sessionId: String, callPath: String) {
        AndroidDatabaseHelper.cancelObserveAnswerFromCallee(
            sessionId,
            callPath)
    }

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (iceCandidate : IceCandidateDTO) -> Unit
    ) {
        AndroidDatabaseHelper.observeIceCandidatesFromCallee(
            sessionId,
            DataConstant.CALL_PATH,
            iceCandidateCallBack)
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer : OfferAnswerDTO) -> Unit
    ) {
        AndroidDatabaseHelper.observeVideoCall(
            sessionId,
            DataConstant.CALL_PATH,
            videoCallCallBack)
    }

}