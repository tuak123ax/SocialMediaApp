package com.minhtu.firesocialmedia.domain.serviceimpl.database

import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance

interface StorageHelperInterface {
    suspend fun saveInstanceToDatabase(
        id: String,
        path: String,
        instance: BaseNewsInstance
    ): Boolean
    suspend fun deleteNewsFromDatabase(path: String, new: NewsDTO)
    suspend fun updateNewsFromDatabase(
        path: String,
        newContent: String,
        newImage: String,
        newVideo: String,
        new: NewsDTO
    ): Boolean
    suspend fun saveNewToDatabase(
        commentId: String,
        path: String,
        instance: NewsDTO
    ): Boolean
    suspend fun saveGroupAndUserGroups(
        groupRootPath: String,
        userRootPath: String,
        userGroupsField: String,
        groupAvatarsStoragePath: String,
        group: GroupDTO,
        userId: String
    ): Boolean
    suspend fun saveNewToGroup(
        newsDTO: NewsDTO,
        groupId: String,
        groupPath: String,
        postsPath: String,
        imagePath: String
    ): Boolean
    suspend fun saveSignUpInformation(user: UserDTO): Boolean
}