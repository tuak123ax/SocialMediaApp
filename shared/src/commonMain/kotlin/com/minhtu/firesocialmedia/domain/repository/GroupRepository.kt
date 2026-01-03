package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance

interface GroupRepository {
    suspend fun saveGroupAndUserGroups(
        group: GroupInstance,
        userId: String): Boolean
    suspend fun getAllGroups(userId : String) : Set<GroupInstance>
}