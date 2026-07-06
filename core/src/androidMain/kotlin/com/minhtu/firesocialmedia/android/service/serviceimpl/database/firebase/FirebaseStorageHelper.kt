package com.minhtu.firesocialmedia.android.service.serviceimpl.database.firebase

import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.minhtu.firesocialmedia.core.constants.Constants
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.AndroidDatabaseHelper.Companion.updateGroupDataOnServer
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.AndroidDatabaseHelper.Companion.uploadMediaAndGetUrl
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.StorageHelperInterface
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class FirebaseStorageHelper : StorageHelperInterface {
    override suspend fun saveInstanceToDatabase(
        commentId: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean = suspendCancellableCoroutine { continuation ->
        Log.d("Task", "saveInstanceToDatabase")
        val storageReference = FirebaseStorage.getInstance().getReference()
            .child(path).child(commentId)
        val databaseReference = FirebaseDatabase.getInstance().getReference()
            .child(path).child(commentId)
        if (instance.image.isNotEmpty()) {
            try {
                val metadata = StorageMetadata.Builder()
                    .setCacheControl("public,max-age=604800,immutable")
                    .build()
                storageReference.putFile(instance.image.toUri(), metadata)
                    .addOnCompleteListener { putFileTask ->
                        if (putFileTask.isSuccessful) {
                            storageReference.downloadUrl.addOnSuccessListener { dataUrl ->
                                instance.updateImage(dataUrl.toString())
                                databaseReference.setValue(instance)
                                    .addOnCompleteListener { addUserTask ->
                                        if (continuation.isActive) continuation.resume(
                                            addUserTask.isSuccessful,
                                            onCancellation = {})
                                    }
                            }
                        }
                    }
            } catch (ex: Exception) {
                logMessage(
                    "saveInstanceToDatabase",
                    { "Exception when send image to Firebase: " + ex.message.toString() })
            }
        } else {
            if (instance.video.isNotEmpty()) {
                try {
                    val metadata = StorageMetadata.Builder()
                        .setCacheControl("public,max-age=604800,immutable")
                        .build()
                    storageReference.putFile(instance.video.toUri(), metadata)
                        .addOnCompleteListener { putFileTask ->
                            if (putFileTask.isSuccessful) {
                                storageReference.downloadUrl.addOnSuccessListener { dataUrl ->
                                    instance.updateVideo(dataUrl.toString())
                                    databaseReference.setValue(instance)
                                        .addOnCompleteListener { addUserTask ->
                                            if (continuation.isActive) continuation.resume(
                                                addUserTask.isSuccessful,
                                                onCancellation = {})
                                        }
                                }
                            }
                        }
                } catch (ex: Exception) {
                    logMessage(
                        "saveInstanceToDatabase",
                        { "Exception when send video to Firebase: " + ex.message.toString() })
                }
            } else {
                databaseReference.setValue(instance).addOnCompleteListener { addNewsTask ->
                    if (continuation.isActive) continuation.resume(
                        addNewsTask.isSuccessful,
                        onCancellation = {})
                }
            }
        }
    }

    override suspend fun deleteNewsFromDatabase(path: String, new: NewsDTO) {
        Log.d("Task", "deleteNewsFromDatabase")

        FirebaseDatabase.getInstance()
            .getReference()
            .child(path)
            .child(new.id)
            .removeValue()
            .await()

        if (new.image.isNotEmpty() || new.video.isNotEmpty()) {
            try {
                FirebaseStorage.getInstance()
                    .getReference()
                    .child(path)
                    .child(new.id)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w("Task", "Storage delete: ${e.message}")
            }
        }
    }

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean {
        Log.d("Task", "updateNewsFromDatabase")

        val dbRef = FirebaseDatabase.getInstance()
            .getReference(path)
            .child(new.id)

        val storageRef = FirebaseStorage.getInstance()
            .getReference(path)
            .child(new.id)

        return try {
            val updates = mutableMapOf<String, Any>("message" to newContent)

            when {
                // Image branch
                newImage.isNotEmpty() -> {
                    if (newImage != new.image) {
                        val metadata = StorageMetadata.Builder()
                            .setCacheControl("public,max-age=604800,immutable")
                            .build()
                        storageRef.putFile(newImage.toUri(), metadata).await()
                        val imageUrl = storageRef.downloadUrl.await().toString()
                        updates["image"] = imageUrl
                        updates["video"] = ""
                    } else {
                        updates["image"] = newImage
                        updates["video"] = ""
                    }
                }

                // Video branch
                newVideo.isNotEmpty() -> {
                    if (newVideo != new.video) {
                        val metadata = StorageMetadata.Builder()
                            .setCacheControl("public,max-age=604800,immutable")
                            .build()
                        storageRef.putFile(newVideo.toUri(), metadata).await()
                        val videoUrl = storageRef.downloadUrl.await().toString()
                        updates["video"] = videoUrl
                        updates["image"] = ""
                    } else {
                        updates["video"] = newVideo
                        updates["image"] = ""
                    }
                }

                // No media: clear both, delete old storage object if any
                else -> {
                    updates["image"] = ""
                    updates["video"] = ""
                    if (new.image.isNotEmpty() || new.video.isNotEmpty()) {
                        runCatching { storageRef.delete().await() }
                    }
                }
            }

            dbRef.updateChildren(updates).await()
            true
        } catch (t: Throwable) {
            Log.e("Task", "updateNewsFromDatabase failed", t)
            false
        }
    }

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean {
        return runCatching {
            val storageRef =
                FirebaseStorage.getInstance().getReference().child(path).child(commentId)
            val dbRef =
                FirebaseDatabase.getInstance().getReference().child(path).child(commentId)

            when {
                instance.image.isNotEmpty() -> {
                    val url = uploadMediaAndGetUrl(
                        storageRef = storageRef,
                        originalUriStr = instance.image,
                        localPath = instance.localPath
                    )
                    instance.updateImage(url)
                }

                instance.video.isNotEmpty() -> {
                    val url = uploadMediaAndGetUrl(
                        storageRef = storageRef,
                        originalUriStr = instance.video,
                        localPath = instance.localPath
                    )
                    instance.updateVideo(url)
                }

                else -> {
                    // No media, just write the post
                }
            }

            dbRef.setValue(instance).await()
            true
        }.getOrElse { e ->
            // optional: log e
            false
        }
    }

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean = suspendCancellableCoroutine { continuation ->

        val databaseRef = FirebaseDatabase.getInstance().reference
        val storageRef = FirebaseStorage.getInstance().reference.child(groupRootPath)
            .child(groupAvatarsStoragePath).child(group.id)

        //Store the avatar to the firebase storage first
        try {
            val metadata = StorageMetadata.Builder()
                .setCacheControl("public,max-age=604800,immutable")
                .build()
            if (group.avatar != Constants.DEFAULT_AVATAR_URL && group.avatar != Constants.DEFAULT_DECADE_AVATAR_URL && group.avatar != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP) {
                storageRef.putFile(group.avatar.toUri(), metadata)
                    .addOnCompleteListener { putFileTask ->
                        if (putFileTask.isSuccessful) {
                            storageRef.downloadUrl.addOnSuccessListener { dataUrl ->
                                //Get the new url of avatar on remote
                                group.avatar = dataUrl.toString()

                                updateGroupDataOnServer(
                                    databaseRef,
                                    groupRootPath,
                                    userRootPath,
                                    userGroupsField,
                                    group,
                                    userId,
                                    continuation
                                )
                            }
                        }
                    }
            } else {
                updateGroupDataOnServer(
                    databaseRef,
                    groupRootPath,
                    userRootPath,
                    userGroupsField,
                    group,
                    userId,
                    continuation
                )
            }
        } catch (ex: Exception) {
            logMessage(
                "saveGroupAndUserGroups",
                { "Exception when save Group And User Groups: " + ex.message.toString() })
        }
    }

    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        return runCatching {
            val storageRef =
                FirebaseStorage.getInstance().getReference().child(groupPath).child(groupId)
                    .child(imagePath).child(newsDTO.id)
            val dbRef =
                FirebaseDatabase.getInstance().getReference().child(groupPath).child(groupId)
                    .child(postsPath).child(newsDTO.id)

            when {
                newsDTO.image.isNotEmpty() -> {
                    val url = uploadMediaAndGetUrl(
                        storageRef = storageRef,
                        originalUriStr = newsDTO.image,
                        localPath = newsDTO.localPath
                    )
                    newsDTO.updateImage(url)
                }

                newsDTO.video.isNotEmpty() -> {
                    val url = uploadMediaAndGetUrl(
                        storageRef = storageRef,
                        originalUriStr = newsDTO.video,
                        localPath = newsDTO.localPath
                    )
                    newsDTO.updateVideo(url)
                }

                else -> {
                    // No media, just write the post
                }
            }

            dbRef.setValue(newsDTO).await()
            true
        }.getOrElse { e ->
            // optional: log e
            false
        }
    }

    override suspend fun saveSignUpInformation(user: UserDTO): Boolean =
        suspendCancellableCoroutine { continuation ->
            val storageReference = FirebaseStorage.getInstance().getReference()
                .child("avatar").child(user.uid)
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.uid)

            if (user.image != Constants.DEFAULT_AVATAR_URL && user.image != Constants.DEFAULT_DECADE_AVATAR_URL && user.image != Constants.DEFAULT_ARK_AVATAR_URL_FOR_GROUP) {
                val metadata = StorageMetadata.Builder()
                    .setCacheControl("public,max-age=604800,immutable")
                    .build()
                storageReference.putFile(user.image.toUri(), metadata)
                    .addOnCompleteListener { putFileTask ->
                        if (putFileTask.isSuccessful) {
                            storageReference.downloadUrl.addOnSuccessListener { avatarUrl ->
                                user.updateImage(avatarUrl.toString())
                                databaseReference.setValue(user)
                                    .addOnCompleteListener { addUserTask ->
                                        if (addUserTask.isSuccessful) {
                                            if (continuation.isActive) continuation.resume(true)
                                        } else {
                                            if (continuation.isActive) continuation.resume(false)
                                        }
                                    }
                            }
                        }
                    }
            } else {
                databaseReference.setValue(user).addOnCompleteListener { addUserTask ->
                    if (addUserTask.isSuccessful) {
                        if (continuation.isActive) continuation.resume(true)
                    } else {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
        }
}