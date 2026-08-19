package com.minhtu.firesocialmedia.data.remote.service.database.supabase

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.group.SupabaseStorage
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper
import com.minhtu.firesocialmedia.storage.group.SupabaseStorageProvider
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.service.database.GroupStorageHelper
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.tasks.await

class GroupSupabaseStorageHelper : GroupStorageHelper {
    private val fileExtensionHelper = SupabaseStorageHelper()

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        val databaseRef = FirebaseDatabase.getInstance().reference

        return try {
            val shouldUploadAvatar =
                group.avatar != SupabaseStorageProvider.DEFAULT_AVATAR_URL &&
                        group.avatar != SupabaseStorageProvider.DEFAULT_DECADE_AVATAR_URL &&
                        group.avatar != SupabaseStorageProvider.DEFAULT_GROUP_AVATAR_URL

            if (shouldUploadAvatar) {
                val extension = fileExtensionHelper.getFileExtension(group.avatar, "jpg")

                val remotePath =
                    "$groupRootPath/$groupAvatarsStoragePath/${group.id}_${System.currentTimeMillis()}.$extension"

                SupabaseStorage.upload(
                    filePath = group.avatar,
                    remotePath = remotePath
                )

                group.avatar = remotePath // store relative path
            }

            updateGroupDataOnServer(
                databaseRef = databaseRef,
                groupRootPath = groupRootPath,
                userRootPath = userRootPath,
                userGroupsField = userGroupsField,
                group = group,
                userId = userId
            )

            true

        } catch (ex: Exception) {
            logMessage(
                "saveGroupAndUserGroups",
                { "Exception with Supabase: ${ex.message}" }
            )
            false
        }
    }

    private suspend fun updateGroupDataOnServer(
        databaseRef: DatabaseReference,
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        return try {
            val groupSummary = GroupSummaryDTO(
                id = group.id,
                name = group.name,
                avatar = group.avatar
            )

            val updates = hashMapOf<String, Any?>(
                "$groupRootPath/${group.id}" to group,
                "$userRootPath/$userId/$userGroupsField/${group.id}" to groupSummary
            )

            databaseRef.updateChildren(updates).await()

            Log.d("Task", "updateChildren SUCCESS")
            true

        } catch (e: Exception) {
            Log.e("Task", "updateChildren FAILED", e)
            false
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
            val dbRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(groupPath)
                .child(groupId)
                .child(postsPath)
                .child(newsDTO.id)

            when {
                newsDTO.image.isNotEmpty() -> {
                    val filePath = newsDTO.localPath.ifEmpty { newsDTO.image }
                    val extension = fileExtensionHelper.getFileExtension(filePath, "jpg")

                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    newsDTO.updateImage(remotePath) // store relative path
                    newsDTO.updateVideo("")
                }

                newsDTO.video.isNotEmpty() -> {
                    val filePath = newsDTO.localPath.ifEmpty { newsDTO.video }
                    val extension = fileExtensionHelper.getFileExtension(filePath, "mp4")

                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${System.currentTimeMillis()}.$extension"

                    SupabaseStorage.upload(
                        filePath = filePath,
                        remotePath = remotePath
                    )

                    newsDTO.updateVideo(remotePath) // store relative path
                    newsDTO.updateImage("")
                }

                else -> {
                    // No media → just save
                }
            }

            dbRef.setValue(newsDTO).await()
            true

        }.getOrElse { e ->
            Log.e("Task", "saveNewToGroup failed: ${e.message}", e)
            false
        }
    }
}
