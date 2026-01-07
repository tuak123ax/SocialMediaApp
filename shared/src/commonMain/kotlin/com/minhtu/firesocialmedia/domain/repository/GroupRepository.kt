package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance

interface GroupRepository {
    suspend fun saveGroupAndUserGroups(
        group: GroupInstance,
        userId: String): Boolean
    suspend fun getAllGroups(userId : String) : Set<GroupInstance>
    suspend fun fetchGroupInfo(groupId: String) : GroupInstance
    suspend fun saveNewToGroup(instance: NewsInstance,
                               groupId : String): Boolean
}