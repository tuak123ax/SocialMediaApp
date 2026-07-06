package com.minhtu.firesocialmedia.ios.service.serviceimpl.database.firebase

import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.StorageHelperInterface
import com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.SupabaseStorageHelper

class FirebaseStorageHelper : StorageHelperInterface {
    private val delegate = SupabaseStorageHelper()

    override suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean = delegate.saveInstanceToDatabase(id, path, instance)

    override suspend fun deleteNewsFromDatabase(path: String, new: NewsDTO) =
        delegate.deleteNewsFromDatabase(path, new)

    override suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean = delegate.updateNewsFromDatabase(path, newContent, newImage, newVideo, new)

    override suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean = delegate.saveNewToDatabase(commentId, path, instance)

    override suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean = delegate.saveGroupAndUserGroups(
        groupRootPath,
        userRootPath,
        userGroupsField,
        groupAvatarsStoragePath,
        group,
        userId
    )

    override suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean = delegate.saveNewToGroup(newsDTO, groupId, groupPath, postsPath, imagePath)

    override suspend fun saveSignUpInformation(user: UserDTO): Boolean =
        delegate.saveSignUpInformation(user)
}