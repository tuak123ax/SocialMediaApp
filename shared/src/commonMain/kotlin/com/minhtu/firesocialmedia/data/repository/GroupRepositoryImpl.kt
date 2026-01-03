package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.group.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.group.toDto
import com.minhtu.firesocialmedia.data.remote.mapper.group.toGroupDTO
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first

class GroupRepositoryImpl(
    private val databaseService: DatabaseService,
    private val networkMonitor: NetworkMonitor
) : GroupRepository {
    override suspend fun saveGroupAndUserGroups(
        group: GroupInstance,
        userId: String
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.saveGroupAndUserGroups(
                DataConstant.GROUP_PATH,
                DataConstant.USER_PATH,
                DataConstant.GROUP_PATH,
                DataConstant.GROUP_AVATAR_STORAGE_PATH,
                group.toDto(),
                userId)
        } else {
            false
        }
    }

    override suspend fun getAllGroups(userId: String): Set<GroupInstance> {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.getAllGroups(
                DataConstant.USER_PATH,
                DataConstant.GROUP_PATH,
                userId
            ).map { it.toGroupDTO().toDomain() }.toSet()
        } else {
            emptySet()
        }
    }
}