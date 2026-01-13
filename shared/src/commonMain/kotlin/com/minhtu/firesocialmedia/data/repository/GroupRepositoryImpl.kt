package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.group.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.group.toDto
import com.minhtu.firesocialmedia.data.remote.mapper.group.toGroupConfigs
import com.minhtu.firesocialmedia.data.remote.mapper.group.toGroupDTO
import com.minhtu.firesocialmedia.data.remote.mapper.news.toDto
import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first

class GroupRepositoryImpl(
    private val databaseService: DatabaseService,
    private val networkMonitor: NetworkMonitor,
    private val clipboardService: ClipboardService
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

    override suspend fun updateNotificationStatus(newStatus: Boolean,
                                                  groupId : String,
                                                  userId : String): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.updateNotificationStatus(
                newStatus,
                groupId,
                userId,
                DataConstant.USER_PATH,
                DataConstant.GROUP_PATH,
                DataConstant.NOTIFICATION_STATUS_PATH
            )
        } else {
            false
        }
    }

    override suspend fun getAllMembersInGroup(groupId: String) : HashMap<String, String> {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.getAllMembersInGroup(
                groupId,
                DataConstant.GROUP_PATH,
                DataConstant.MEMBERS_PATH
            )
        } else {
            HashMap()
        }
    }

    override suspend fun getGroupConfigs(userId: String,
                                         groupId: String): GroupConfigs {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.getGroupConfigs(
                userId,
                groupId,
                DataConstant.USER_PATH,
                DataConstant.GROUP_PATH
            ).toGroupConfigs()
        } else {
            GroupConfigs()
        }
    }

    override suspend fun fetchNotificationState(
        userId: String,
        groupId: String
    ): Boolean {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            databaseService.fetchNotificationState(
                userId,
                groupId,
                DataConstant.USER_PATH,
                DataConstant.GROUP_PATH,
                DataConstant.NOTIFICATION_STATUS_PATH
            )
        } else {
            false
        }
    }

    override suspend fun copyLink(copyData: String) {
        clipboardService.copy(copyData)
    }
}