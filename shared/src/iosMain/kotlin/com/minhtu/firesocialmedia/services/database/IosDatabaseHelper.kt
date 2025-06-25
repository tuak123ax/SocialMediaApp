package com.minhtu.firesocialmedia.services.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageReference
import cocoapods.FirebaseStorage.FIRStorageTaskStatusFailure
import cocoapods.FirebaseStorage.FIRStorageTaskStatusSuccess
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.CommentInstance
import com.minhtu.firesocialmedia.data.model.NewsInstance
import com.minhtu.firesocialmedia.data.model.NotificationInstance
import com.minhtu.firesocialmedia.data.model.fromMap
import com.minhtu.firesocialmedia.data.model.toMap
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataTaskWithURL
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithFloat
import platform.Foundation.numberWithInt
import platform.Foundation.writeToURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class IosDatabaseHelper {
    companion object {
        private val database: FIRDatabaseReference = FIRDatabase.database().reference()
        private val storage: FIRStorageReference = FIRStorage.storage().reference()

        @OptIn(ExperimentalEncodingApi::class)
        suspend fun saveInstanceToDatabase(
            id: String,
            path: String,
            instance: NewsInstance,
            stateFlow : MutableStateFlow<Boolean?>
        ) {
            val storageReference = storage.child(path).child(id)
            val databaseReference = database.child(path).child(id)

            try {
                if (instance.image.isNotEmpty()) {
                    val nsDataImage = Base64.decode(instance.image).toNSData()
                    val metadata = FIRStorageMetadata().apply {
                        setContentType("image/jpeg")
                    }

                    val imageUrl = uploadAndGetRemoteURL(storageReference, nsDataImage, metadata)
                    logMessage("saveInstanceToDatabase", imageUrl)
                    instance.updateImage(imageUrl)
                } else {
                    if(instance.video.isNotEmpty()) {
                        val nsDataVideo = Base64.decode(instance.video).toNSData()
                        val metadata = FIRStorageMetadata().apply {
                            setContentType("video/mp4")
                        }

                        val videoUrl = uploadAndGetRemoteURL(storageReference, nsDataVideo, metadata)
                        logMessage("saveInstanceToDatabase", videoUrl)
                        instance.updateVideo(videoUrl)
                    }
                }
                // Convert instance to Firebase-compatible Map
                val newMap = instance.toMap() as NSDictionary
                // Save user object in Realtime Database
                setValue(databaseReference, newMap)
                stateFlow.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                stateFlow.value = false
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        suspend fun saveInstanceToDatabase(
            id: String,
            path: String,
            instance: CommentInstance,
            stateFlow : MutableStateFlow<Boolean?>
        ) {
            val storageReference = storage.child(path).child(id)
            val databaseReference = database.child(path).child(id)

            try {
                if (instance.image.isNotEmpty()) {
                    val nsDataImage = Base64.decode(instance.image).toNSData()
                    val metadata = FIRStorageMetadata().apply {
                        setContentType("image/jpeg")
                    }

                    val imageUrl = uploadAndGetRemoteURL(storageReference, nsDataImage, metadata)
                    logMessage("saveInstanceToDatabase", imageUrl)
                    instance.updateImage(imageUrl)
                }
                // Convert instance to Firebase-compatible Map
                val commentMap = instance.toMap() as NSDictionary
                logMessage("saveInstanceToDatabase", "After convert to NSDictionary: $commentMap")
                // Save user object in Realtime Database
                setValue(databaseReference, commentMap)
                stateFlow.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                stateFlow.value = false
            }
        }

        suspend fun saveValueToDatabase(
            id: String,
            path: String,
            value: Map<String, Int>,
            externalPath: String
        ) {
            try{
                val ref = database.child(path).child(id)
                    .let { if (externalPath.isNotEmpty()) it.child(externalPath) else it }
                if (value.isNotEmpty()) {
                    setValue(ref, value)
                } else {
                    removeValue(ref)
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        suspend fun saveListToDatabase(
            id: String,
            path: String,
            value: List<String>,
            externalPath: String
        ) {
            try{
                val ref = database.child(path).child(id)
                    .let { if (externalPath.isNotEmpty()) it.child(externalPath) else it }

                setValue(ref, value)
            } catch (e : Exception){
                e.printStackTrace()
            }
        }

        suspend fun saveStringToDatabase(
            id: String,
            path: String,
            value: String,
            externalPath: String
        ) {
            try{
                if (value.isNotEmpty()) {
                    val ref = database.child(path).child(id)
                        .let { if (externalPath.isNotEmpty()) it.child(externalPath) else it }
                    setValue(ref, value)
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        suspend fun updateCountValueInDatabase(
            id: String,
            path: String,
            externalPath: String,
            value: Int
        ) {
            try {
                if (value >= 0) {
                    val ref = database.child(path).child(id)
                        .let { if (externalPath.isNotEmpty()) it.child(externalPath) else it }
                    setValue(ref, value)
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        suspend fun saveNotificationToDatabase(
            id: String,
            path: String,
            instance: List<NotificationInstance>
        ) {
            try{
                val ref = database.child(path).child(id).child(Constants.NOTIFICATION_PATH)
                setValue(ref, instance.map { it.toMap() })
            } catch(e : Exception){
                e.printStackTrace()
            }
        }

        suspend fun deleteNotificationFromDatabase(
            id: String,
            path: String,
            notification: NotificationInstance
        ) {
            try {
                val ref = database.child(path).child(id).child(Constants.NOTIFICATION_PATH)
                val snapshot = getValue(ref)
                val list = (snapshot as? List<*>)?.mapNotNull { it as? Map<String, Any> }
                    ?.mapNotNull { NotificationInstance.fromMap(it) }?.toMutableList()
                list?.remove(notification)
                setValue(ref, list!!.map { it.toMap() })
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        suspend fun deleteNewsFromDatabase(path: String, new: NewsInstance) {
            try {
                val dbRef = database.child(path).child(new.id)
                val storageRef = storage.child(path).child(new.id)
                removeValue(dbRef)
                if (new.image.isNotEmpty()) delete(storageRef)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        suspend fun deleteCommentFromDatabase(path: String, comment: CommentInstance) {
            try {
                val dbRef = database.child(path).child(comment.id)
                removeValue(dbRef)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        suspend fun updateNewsFromDatabase(
            path: String,
            newContent: String,
            newImage: String,
            newVideo : String,
            news: NewsInstance,
            stateFlow : MutableStateFlow<Boolean?>
        ) {
            logMessage("updateNewsFromDatabase", newImage)
            try {
                val storageReference = storage.child(path).child(news.id)
                var updates = mutableMapOf<String, Any>("message" to newContent)
                if(newImage.isNotEmpty()){
                    if(newImage != news.image) {
                        val nsDataImage = Base64.decode(newImage).toNSData()
                        val metadata = FIRStorageMetadata().apply {
                            setContentType("image/jpeg")
                        }

                        val imageUrl = uploadAndGetRemoteURL(storageReference, nsDataImage, metadata)
                        logMessage("updateNewsFromDatabase", imageUrl)
                        updates["image"] = imageUrl
                    } else {
                        updates = mutableMapOf<String, Any>(
                            "message" to newContent,
                            "image" to newImage
                        )
                    }
                } else {
                    if(newVideo.isNotEmpty()) {
                        if(newVideo != news.video) {
                            val nsDataVideo = Base64.decode(newVideo).toNSData()
                            val metadata = FIRStorageMetadata().apply {
                                setContentType("video/mp4")
                            }

                            val videoUrl = uploadAndGetRemoteURL(storageReference, nsDataVideo, metadata)
                            logMessage("updateNewsFromDatabase", videoUrl)
                            updates["video"] = videoUrl
                        } else {
                            updates = mutableMapOf<String, Any>(
                                "message" to newContent,
                                "video" to newVideo
                            )
                        }
                    } else {
                        updates = mutableMapOf<String, Any>(
                            "message" to newContent,
                            "image" to "",
                            "video" to ""
                        )
                        if(news.image.isNotEmpty() || news.video.isNotEmpty()) {
                            delete(storageReference)
                        }
                    }
                }
                updateChildren(database.child(path).child(news.id), updates)
                stateFlow.value = true
            } catch (e: Throwable) {
                e.printStackTrace()
                stateFlow.value = false
            }
        }

        private suspend fun setValue(ref: FIRDatabaseReference, value: Any): Boolean =
            suspendCancellableCoroutine { cont ->
                val preparedValue = prepareValueForFirebase(value)
                ref.setValue(preparedValue) { error, _ ->
                    if (error == null) cont.resume(true)
                    else cont.resumeWithException(Throwable(error.localizedDescription))
                }
            }

        @OptIn(BetaInteropApi::class)
        private fun prepareValueForFirebase(value: Any): Any {
            return when (value) {
                is Map<*, *> -> {
                    val dict = NSMutableDictionary()
                    value.forEach { (key, v) ->
                        if (key is String && v != null) {
                            dict.setObject(prepareValueForFirebase(v), key.toNSString())
                        }
                    }
                    dict
                }
                is List<*> -> {
                    val array = NSMutableArray()
                    value.forEach { item ->
                        if (item != null) {
                            array.addObject(prepareValueForFirebase(item))
                        }
                    }
                    array
                }
                is Boolean -> NSNumber.numberWithBool(value)
                is Int -> NSNumber.numberWithInt(value)
                is Double -> NSNumber.numberWithDouble(value)
                is Float -> NSNumber.numberWithFloat(value)
                is String -> NSString.create(string = value)
                else -> value
            }
        }

        @OptIn(BetaInteropApi::class)
        fun String.toNSString(): NSString = NSString.create(string = this)

        private suspend fun removeValue(ref: FIRDatabaseReference): Boolean =
            suspendCancellableCoroutine { cont ->
                ref.removeValueWithCompletionBlock { error, _ ->
                    if (error == null) cont.resume(true)
                    else cont.resumeWithException(Throwable(error.localizedDescription))
                }
            }

        private suspend fun updateChildren(
            ref: FIRDatabaseReference,
            updates: Map<String, Any>
        ): Boolean = suspendCancellableCoroutine { cont ->
            // Cast Map<String, Any> to Map<Any?, *>
            val castedUpdates: Map<Any?, *> = updates.entries.associate { it.key as Any? to it.value }

            ref.updateChildValues(castedUpdates) { error, _ ->
                if (error == null) cont.resume(true)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }

        private suspend fun getValue(ref: FIRDatabaseReference): Any? =
            suspendCancellableCoroutine { cont ->
                ref.observeSingleEventOfType(
                    FIRDataEventType.FIRDataEventTypeValue
                ) { snapshot: FIRDataSnapshot?, previousSiblingKey: String? ->
                    if (snapshot != null) {
                        cont.resume(snapshot.value)
                    } else {
                        cont.resume(null)
                    }
                }
            }

        private suspend fun delete(ref: FIRStorageReference): Boolean =
            suspendCancellableCoroutine { cont ->
                ref.deleteWithCompletion { error ->
                    if (error == null) cont.resume(true)
                    else cont.resumeWithException(Throwable(error.localizedDescription))
                }
            }

        fun downloadImage(imageUrl: String, fileName: String, onComplete: (Boolean) -> Unit) {
            val url = NSURL(string = imageUrl)

            val session = NSURLSession.sharedSession

            val task = session.dataTaskWithURL(url) { data, response, error ->
                if (error != null || data == null) {
                    onComplete(false)
                    return@dataTaskWithURL
                }

                val paths = NSSearchPathForDirectoriesInDomains(
                    directory = NSDocumentDirectory,
                    domainMask = NSUserDomainMask,
                    expandTilde = true
                )

                val documentsDirectory = paths.firstOrNull() as? String
                if (documentsDirectory == null) {
                    onComplete(false)
                    return@dataTaskWithURL
                }

                val filePath = "$documentsDirectory/$fileName"
                val fileUrl = NSURL.fileURLWithPath(path = filePath)

                val success = data.writeToURL(fileUrl, atomically = true)
                saveImageToPhotos(data)
                onComplete(success)
            }

            task.resume()
        }

        fun saveImageToPhotos(data: NSData) {
            val image = UIImage(data = data)
            UIImageWriteToSavedPhotosAlbum(image, null, null, null)
        }

        suspend fun uploadAndGetRemoteURL(storageReference : FIRStorageReference, data: NSData, metadata: FIRStorageMetadata) : String{
            try {
                // Upload NSData directly
                val uploadTask = storageReference.putData(data, metadata)

                // Await upload completion
                suspendCancellableCoroutine<Unit> { cont ->
                    uploadTask.observeStatus(FIRStorageTaskStatusSuccess) {
                        cont.resume(Unit, onCancellation = {})
                    }
                    uploadTask.observeStatus(FIRStorageTaskStatusFailure) { taskSnapshot ->
                        val error = taskSnapshot?.error()
                        cont.resumeWithException(Throwable(error?.localizedDescription ?: "Upload failed"))
                    }
                }

                // Get download URL
                return suspendCancellableCoroutine { cont ->
                    storageReference.downloadURLWithCompletion { url, error ->
                        when {
                            error != null -> cont.resumeWithException(Throwable(error.localizedDescription))
                            url != null -> cont.resume(url.absoluteString.toString(), onCancellation = {})
                            else -> cont.resumeWithException(Throwable("Unknown error getting download URL"))
                        }
                    }
                }
            } catch(e : Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}

