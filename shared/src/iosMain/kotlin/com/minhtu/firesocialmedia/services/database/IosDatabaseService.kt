package com.minhtu.firesocialmedia.services.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageReference
import cocoapods.FirebaseStorage.FIRStorageMetadata
import com.minhtu.firesocialmedia.DatabaseService
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.instance.fromMap
import com.minhtu.firesocialmedia.instance.toMap
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.services.crypto.IosCryptoHelper
import com.minhtu.firesocialmedia.toNSData
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
        externalPath: String
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

    override fun getAllUsers(path: String, callback: Utils.Companion.GetUserCallback) {
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


    override fun getAllNews(
        path: String,
        callback: Utils.Companion.GetNewCallback
    ) {
        val result = mutableListOf<NewsInstance>()
        val databaseReference = FIRDatabase.database().reference().child(path)

        databaseReference.observeSingleEventOfType(
            FIRDataEventType.FIRDataEventTypeValue,
            withBlock = { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    result.clear()
                    val children = snapshot.children
                    while (true) {
                        val child = children.nextObject() as? FIRDataSnapshot ?: break
                        val rawValue = child.value as? Map<*, *> ?: continue

                        // Cast Map<*, *> to Map<String, Any?>
                        val value = rawValue.entries.associate {
                            (it.key as? String) to it.value
                        }.filterKeys { it != null } as Map<String, Any?>

                        try {
                            val news = value.toNewsInstance()
                            result.add(news)
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



    override fun getAllComments(
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
                            logMessage("allComment", comment.id + ": "+ comment.listReplies.size)
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


    override fun getAllNotificationsOfUser(
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
                    result.clear()
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
        )
    }


    override suspend fun saveListToDatabase(
        id: String,
        path: String,
        value: ArrayList<String>,
        externalPath: String
    ) {
        IosDatabaseHelper.saveListToDatabase(id,path,value,externalPath)
    }

    override fun downloadImage(image: String, fileName: String, onComplete: (Boolean) -> Unit) {
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
                    logMessage("saveSignUpInformation", e.message.toString())
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

}
