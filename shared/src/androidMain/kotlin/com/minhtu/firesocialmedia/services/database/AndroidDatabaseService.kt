package com.minhtu.firesocialmedia.services.database

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.CommentInstance
import com.minhtu.firesocialmedia.data.model.NewsInstance
import com.minhtu.firesocialmedia.data.model.NotificationInstance
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.di.DatabaseService
import com.minhtu.firesocialmedia.services.crypto.AndroidCryptoHelper
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidDatabaseService(private val context: Context) : DatabaseService{
    override suspend fun updateFCMTokenForCurrentUser(currentUser : UserInstance) {
        val secureSharedPreferences = AndroidCryptoHelper.getEncryptedSharedPreferences(context)
        val currentFCMToken = secureSharedPreferences.getString(Constants.KEY_FCM_TOKEN, "")
        if(!currentFCMToken.isNullOrEmpty()) {
            if(currentUser.token != currentFCMToken) {
                currentUser.token = currentFCMToken
                AndroidDatabaseHelper.saveStringToDatabase(currentUser.uid,Constants.USER_PATH, currentFCMToken, Constants.TOKEN_PATH)
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
        AndroidDatabaseHelper.saveValueToDatabase(id,
            path,
            value,
            externalPath,
            callback)
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
        new: NewsInstance
    ) {
        AndroidDatabaseHelper.deleteNewsFromDatabase(path, new)
    }

    override suspend fun deleteCommentFromDatabase(
        path: String,
        comment: CommentInstance
    ) {
        AndroidDatabaseHelper.deleteCommentFromDatabase(path, comment)
    }

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: NewsInstance,
        stateFlow: MutableStateFlow<Boolean?>
    ) {
        AndroidDatabaseHelper.saveInstanceToDatabase(
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
        AndroidDatabaseHelper.saveInstanceToDatabase(
            id,
            path,
            instance,
            stateFlow)
    }

    override fun getAllUsers(path: String, callback: Utils.Companion.GetUserCallback){
        val result = ArrayList<UserInstance>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference().child(Constants.USER_PATH)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val user: UserInstance? = dataSnapshot.getValue(UserInstance::class.java)
                    if (user != null) {
                        result.add(user)
                    }
                }
                callback.onSuccess(result)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure()
            }
        })
    }

    override fun getAllNews(
        path: String,
        callback: Utils.Companion.GetNewCallback
    ) {
        val result = ArrayList<NewsInstance>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference().child(path)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val news: NewsInstance? = dataSnapshot.getValue(NewsInstance::class.java)
                    if (news != null) {
                        result.add(news)
                    }
                }
                callback.onSuccess(result)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure()
            }
        })
    }

    override fun getAllComments(
        path: String,
        newsId : String,
        callback: Utils.Companion.GetCommentCallback
    ) {
        val result = ArrayList<CommentInstance>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference()
            .child(Constants.NEWS_PATH)
            .child(newsId)
            .child(path)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val comments: CommentInstance? = dataSnapshot.getValue(CommentInstance::class.java)
                    if (comments != null) {
                        result.add(comments)
                    }
                }
                callback.onSuccess(result)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure()
            }
        })
    }

    override fun getAllNotificationsOfUser(
        path: String,
        currentUserUid : String,
        callback: Utils.Companion.GetNotificationCallback
    ) {
        val result = ArrayList<NotificationInstance>()
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference().child(Constants.USER_PATH)
            .child(currentUserUid).child(path)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for (dataSnapshot in snapshot.getChildren()) {
                    val notification = dataSnapshot.getValue(NotificationInstance::class.java)
                    if (notification != null) {
                        result.add(notification)
                    }
                }
                callback.onSuccess(result)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure()
            }
        })
    }

    override suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    ) {
        AndroidDatabaseHelper.saveListToDatabase(id,path,value,externalPath)
    }

    override fun downloadImage(image: String, fileName: String, onComplete: (Boolean) -> Unit) {
        AndroidDatabaseHelper.downloadImage(context, image, fileName, onComplete)
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo : String,
        new: NewsInstance,
        status: MutableStateFlow<Boolean?>
    ) {
        AndroidDatabaseHelper.updateNewsFromDatabase(path,newContent,newImage,newVideo,new,status)
    }

    override suspend fun saveSignUpInformation(user : UserInstance,
                                               callBack: Utils.Companion.SaveSignUpInformationCallBack) {
        val storageReference = FirebaseStorage.getInstance().getReference()
            .child("avatar").child(user.uid)
        val databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(user.uid)

        if(user.image != Constants.DEFAULT_AVATAR_URL){
            storageReference.putFile(user.image.toUri()).addOnCompleteListener{ putFileTask ->
                if(putFileTask.isSuccessful){
                    storageReference.downloadUrl.addOnSuccessListener { avatarUrl ->
                        user.updateImage(avatarUrl.toString())
                        databaseReference.setValue(user).addOnCompleteListener{addUserTask ->
                            if(addUserTask.isSuccessful){
                                callBack.onSuccess()
                            } else {
                                callBack.onFailure()
                            }
                        }
                    }
                }
            }
        } else {
            databaseReference.setValue(user).addOnCompleteListener{addUserTask ->
                if(addUserTask.isSuccessful){
                    callBack.onSuccess()
                } else {
                    callBack.onFailure()
                }
            }
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationInstance>
    ) {
        AndroidDatabaseHelper.saveNotificationToDatabase(id,path,instance)
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationInstance
    ) {
        AndroidDatabaseHelper.deleteNotificationFromDatabase(id, path, notification)
    }

    override fun sendOfferToFireBase(
        sessionId : String,
        offer : OfferAnswer,
        callPath : String,
        sendOfferCallBack : Utils.Companion.BasicCallBack) {
        AndroidDatabaseHelper.sendOfferToFireBase(
            sessionId,
            offer,
            callPath,
            sendOfferCallBack
        )
    }

    override fun sendIceCandidateToFireBase(
        sessionId : String,
        iceCandidate: IceCandidateData,
        whichCandidate : String,
        callPath : String,
        sendIceCandidateCallBack : Utils.Companion.BasicCallBack) {
        AndroidDatabaseHelper.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate,
            whichCandidate,
            callPath,
            sendIceCandidateCallBack)
    }

    override fun sendCallSessionToFirebase(session: AudioCallSession,
                                           callPath : String,
                                           sendCallSessionCallBack : Utils.Companion.BasicCallBack) {
        AndroidDatabaseHelper.sendCallSessionToFirebase(session, callPath, sendCallSessionCallBack)
    }

    override fun deleteCallSession(
        sessionId: String,
        callPath: String,
        deleteCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.deleteCallSession(sessionId, callPath, deleteCallBack)
    }

    override fun observePhoneCall(
        isInCall : MutableStateFlow<Boolean>,
        currentUserId: String,
        callPath: String,
        phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
        endCallSession: (Boolean) -> Unit,
        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit) {
        AndroidDatabaseHelper.observePhoneCall(
            isInCall,
            currentUserId,
            callPath,
            phoneCallCallBack,
            endCallSession,
            iceCandidateCallBack)
    }

    override fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswer,
        callPath: String,
        sendAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidDatabaseHelper.sendAnswerToFireBase(
            sessionId,
            answer,
            callPath,
            sendAnswerCallBack
        )
    }

    override fun isCalleeInActiveCall(
        calleeId: String,
        callPath: String,
        onResult: (Boolean) -> Unit
    ) {
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
            onResult(isBusy)
        }.addOnFailureListener {
            onResult(false)
        }
    }

    override fun observeAnswerFromCallee(
        sessionId: String,
        callPath: String,
        answerCallBack: (answer : OfferAnswer) -> Unit
    ) {
        AndroidDatabaseHelper.observeAnswerFromCallee(
            sessionId,
            callPath,
            answerCallBack)
    }

    override fun cancelObserveAnswerFromCallee(sessionId: String, callPath: String) {
        AndroidDatabaseHelper.cancelObserveAnswerFromCallee(
            sessionId,
            callPath)
    }

    override fun observeIceCandidatesFromCallee(
        sessionId: String,
        callPath: String,
        iceCandidateCallBack: (iceCandidate : IceCandidateData) -> Unit
    ) {
        AndroidDatabaseHelper.observeIceCandidatesFromCallee(
            sessionId,
            callPath,
            iceCandidateCallBack)
    }

    override fun observeVideoCall(
        sessionId: String,
        callPath: String,
        videoCallCallBack: (offer : OfferAnswer) -> Unit
    ) {
        AndroidDatabaseHelper.observeVideoCall(
            sessionId,
            callPath,
            videoCallCallBack)
    }
}