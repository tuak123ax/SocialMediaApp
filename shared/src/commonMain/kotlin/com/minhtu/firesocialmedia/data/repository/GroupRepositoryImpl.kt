package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.group.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.group.toDto
import com.minhtu.firesocialmedia.data.remote.mapper.group.toGroupDTO
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
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

    override suspend fun fetchGroupInfo(groupId: String): GroupInstance {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            val groupDTO = databaseService.fetchGroupInfo(
                groupId,
                DataConstant.GROUP_PATH)?.toDomain()
            groupDTO ?: GroupInstance()
        } else {
            GroupInstance()
        }
    }

    override suspend fun saveNewToGroup(instance: NewsInstance,
                                        groupId : String): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.saveNewToGroup(
                instance.toDto(),
                groupId,
                DataConstant.GROUP_PATH,
                DataConstant.POSTS_PATH,
                DataConstant.IMAGE_PATH
            )
        } else {
            false
        }
    }
}