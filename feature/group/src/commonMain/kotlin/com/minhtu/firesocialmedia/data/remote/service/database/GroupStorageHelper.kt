package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.group.data.remote.dto.news.NewsDTO

interface GroupStorageHelper {
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
}
