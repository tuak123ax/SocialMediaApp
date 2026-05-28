package com.minhtu.firesocialmedia.domain.serviceimpl.database.firebase

import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.toMap
import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.serviceimpl.database.IosDatabaseHelper
import com.minhtu.firesocialmedia.domain.serviceimpl.database.StorageHelperInterface
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.platform.toNSData
import platform.Foundation.NSDictionary
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class FirebaseStorageHelper : StorageHelperInterface {
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean {
        val storageRef = storage.child(path).child(id)
        val dbRef = IosDatabaseHelper.Companion.database.child(path).child(id)

        return try {
            when {
                instance.image.isNotEmpty() -> {
                    // Upload image, set URL back
                    val data = Base64.Default.decode(instance.image).toNSData()
                    val metadata = FIRStorageMetadata().apply {
                        setContentType("image/jpeg")
                        setCacheControl("public,max-age=604800,immutable")
                    }
                    val imageUrl = IosDatabaseHelper.Companion.uploadAndGetRemoteURL(
                        storageRef,
                        data,
                        metadata
                    )
                    logMessage("saveInstanceToDatabase") { "imageUrl=$imageUrl" }
                    instance.updateImage(imageUrl)

                    val map = instance.toMap() as NSDictionary
                    IosDatabaseHelper.Companion.setValue(
                        dbRef,
                        map
                    )  // suspend, should throw on failure
                    true
                }

                instance.video.isNotEmpty() -> {
                    // Upload video, set URL back
                    val data = Base64.Default.decode(instance.video).toNSData()
                    val metadata = FIRStorageMetadata().apply {
                        setContentType("video/mp4")
                        setCacheControl("public,max-age=604800,immutable")
                    }
                    val videoUrl = IosDatabaseHelper.Companion.uploadAndGetRemoteURL(
                        storageRef,
                        data,
                        metadata
                    )
                    logMessage("saveInstanceToDatabase") { "videoUrl=$videoUrl" }
                    instance.updateVideo(videoUrl)

                    val map = instance.toMap() as NSDictionary
                    IosDatabaseHelper.Companion.setValue(dbRef, map)
                    true
                }

                else -> {
                    // No media: write instance as is
                    val map = instance.toMap() as NSDictionary
                    IosDatabaseHelper.Companion.setValue(dbRef, map)
                    true
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteNewsFromDatabase(path: String, new: NewsDTO) {
        try {
            val dbRef = IosDatabaseHelper.Companion.database.child(path).child(new.id)
            val storageRef = storage.child(path).child(new.id)
            IosDatabaseHelper.Companion.removeValue(dbRef)
            if (new.image.isNotEmpty()) IosDatabaseHelper.Companion.delete(storageRef)
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
        news: NewsDTO,
    ) : Boolean {
        logMessage("updateNewsFromDatabase") { newImage }
        try {
            val storageReference = storage.child(path).child(news.id)
            var updates = mutableMapOf<String, Any>("message" to newContent)
            if(newImage.isNotEmpty()){
                if(newImage != news.image) {
                    val nsDataImage = Base64.Default.decode(newImage).toNSData()
                    val metadata = FIRStorageMetadata().apply {
                        setContentType("image/jpeg")
                        setCacheControl("public,max-age=604800,immutable")
                    }

                    val imageUrl = IosDatabaseHelper.Companion.uploadAndGetRemoteURL(
                        storageReference,
                        nsDataImage,
                        metadata
                    )
                    logMessage("updateNewsFromDatabase") { imageUrl }
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
                        val nsDataVideo = Base64.Default.decode(newVideo).toNSData()
                        val metadata = FIRStorageMetadata().apply {
                            setContentType("video/mp4")
                            setCacheControl("public,max-age=604800,immutable")
                        }

                        val videoUrl = IosDatabaseHelper.Companion.uploadAndGetRemoteURL(
                            storageReference,
                            nsDataVideo,
                            metadata
                        )
                        logMessage("updateNewsFromDatabase") { videoUrl }
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
                        IosDatabaseHelper.Companion.delete(storageReference)
                    }
                }
            }
            IosDatabaseHelper.Companion.updateChildren(
                IosDatabaseHelper.Companion.database.child(
                    path
                ).child(news.id), updates
            )
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return saveInstanceToDatabase(
            commentId,
            path,
            instance
        )
    }

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean {
        val storageReference: FIRStorageReference = FIRStorage.storage().reference().child("avatar").child(user.uid)
        val databaseReference: FIRDatabaseReference = FIRDatabase.Companion.database().reference().child("users").child(user.uid)
        try {
            if (user.image != Constants.Companion.DEFAULT_AVATAR_URL &&
                user.image != Constants.Companion.DEFAULT_DECADE_AVATAR_URL &&
                user.image != Constants.Companion.DEFAULT_ARK_AVATAR_URL_FOR_GROUP) {
                val nsDataAvatar = Base64.Default.decode(user.image).toNSData()
                val metadata = FIRStorageMetadata().apply {
                    setContentType("image/jpeg")
                    setCacheControl("public,max-age=604800,immutable")
                }

                try{
                    val avatarRemoteUrl = IosDatabaseHelper.Companion.uploadAndGetRemoteURL(storageReference,nsDataAvatar,metadata)
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
}