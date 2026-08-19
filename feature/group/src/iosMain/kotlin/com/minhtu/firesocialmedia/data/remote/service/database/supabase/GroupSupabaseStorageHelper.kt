package com.minhtu.firesocialmedia.data.remote.service.database.supabase

import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import com.minhtu.firesocialmedia.storage.group.SupabaseStorageProvider
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.service.database.GroupStorageHelper
import com.minhtu.firesocialmedia.ios.service.serviceimpl.database.supabase.group.SupabaseStorage
import com.minhtu.firesocialmedia.platform.getCurrentTime
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class GroupSupabaseStorageHelper : GroupStorageHelper {
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean {
        return try {
            val shouldUploadAvatar =
                group.avatar != SupabaseStorageProvider.DEFAULT_AVATAR_URL &&
                group.avatar != SupabaseStorageProvider.DEFAULT_DECADE_AVATAR_URL &&
                group.avatar != SupabaseStorageProvider.DEFAULT_GROUP_AVATAR_URL

            if (shouldUploadAvatar) {
                val bytes = Base64.Default.decode(group.avatar)
                val remotePath =
                    "$groupRootPath/$groupAvatarsStoragePath/${group.id}_${getCurrentTime()}.jpg"
                SupabaseStorage.uploadBytes(bytes, remotePath)
                group.avatar = remotePath
            }
            updateGroupDataOnServer(
                dbRoot = FIRDatabase.database().reference(),
                groupRootPath = groupRootPath,
                userRootPath = userRootPath,
                userGroupsField = userGroupsField,
                group = group,
                userId = userId
            )
            true
        } catch (e: Exception) {
            logMessage("GroupSupabaseStorageHelper", { "saveGroupAndUserGroups failed: ${e.message}" })
            false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean {
        return runCatching {
            val dbRef = FIRDatabase.database().reference()
                .child(groupPath).child(groupId).child(postsPath).child(newsDTO.id)
            when {
                newsDTO.image.isNotEmpty() -> {
                    val sourceBase64 = newsDTO.localPath.ifEmpty { newsDTO.image }
                    val bytes = Base64.Default.decode(sourceBase64)
                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${getCurrentTime()}.jpg"
                    SupabaseStorage.uploadBytes(bytes, remotePath)
                    newsDTO.updateImage(remotePath)
                    newsDTO.updateVideo("")
                }
                newsDTO.video.isNotEmpty() -> {
                    val sourcePath = newsDTO.localPath.ifEmpty { newsDTO.video }
                    val remotePath =
                        "$groupPath/$groupId/$imagePath/${newsDTO.id}_${getCurrentTime()}.mp4"
                    SupabaseStorage.uploadFile(sourcePath, remotePath)
                    newsDTO.updateVideo(remotePath)
                    newsDTO.updateImage("")
                }
                else -> { /* no media */ }
            }
            setValueSuspend(dbRef, newsDTO.toMap())
            true
        }.getOrElse { e ->
            logMessage("GroupSupabaseStorageHelper", { "saveNewToGroup failed: ${e.message}" })
            false
        }
    }

    private suspend fun updateGroupDataOnServer(
        dbRoot: FIRDatabaseReference,
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        group: GroupDTO,
        userId: String
    ) {
        val groupSummary = GroupSummaryDTO(id = group.id, name = group.name, avatar = group.avatar)

        val groupMap: Map<String, Any?> = mapOf(
            "id" to group.id,
            "name" to group.name,
            "avatar" to group.avatar,
            "password" to group.password,
            "description" to group.description,
            "createdDate" to group.createdDate,
            "memberCount" to group.memberCount,
            "members" to group.members
        )
        val summaryMap: Map<String, Any?> = mapOf(
            "id" to groupSummary.id,
            "name" to groupSummary.name,
            "avatar" to groupSummary.avatar,
            "notificationOn" to groupSummary.notificationOn
        )

        val updates: Map<String, Any?> = mapOf(
            "$groupRootPath/${group.id}" to groupMap,
            "$userRootPath/$userId/$userGroupsField/${group.id}" to summaryMap
        )
        val castedUpdates: Map<Any?, *> = updates.entries.associate { it.key as Any? to it.value }
        suspendCancellableCoroutine<Unit> { cont ->
            dbRoot.updateChildValues(castedUpdates) { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }
    }

    private suspend fun setValueSuspend(ref: FIRDatabaseReference, value: Map<String, Any?>) =
        suspendCancellableCoroutine<Unit> { cont ->
            ref.setValue(value) { error, _ ->
                if (error == null) cont.resume(Unit)
                else cont.resumeWithException(Throwable(error.localizedDescription))
            }
        }
}
