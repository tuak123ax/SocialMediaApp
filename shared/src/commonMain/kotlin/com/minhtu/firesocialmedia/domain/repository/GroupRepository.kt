package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface GroupRepository {
    suspend fun saveGroupAndUserGroups(
        group: GroupInstance,
        userId: String): Boolean
    suspend fun getAllGroups(userId : String) : Set<GroupInstance>
    suspend fun fetchGroupInfo(groupId: String) : GroupInstance
    suspend fun saveNewToGroup(instance: NewsInstance,
                               groupId : String): Boolean

    suspend fun updateNotificationStatus(newStatus: Boolean,
                                         groupId : String,
                                         userId : String): Boolean

    suspend fun getAllMembersInGroup(groupId: String) : HashMap<String, String>
    suspend fun getGroupConfigs(userId: String,
                                groupId: String): GroupConfigs

    suspend fun fetchNotificationState(userId: String, groupId: String): Boolean
    suspend fun copyLink(copyData: String)
    suspend fun inviteFriendToGroup(
        friend: UserInstance)

    suspend fun joinGroup(user: UserInstance,
                          group : GroupInstance): Boolean

    suspend fun leaveGroup(user: UserInstance, group: GroupInstance): Boolean
    suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) : Boolean
    suspend fun removeMember(member: UserInstance,
                             group : GroupInstance): Boolean

    suspend fun promoteMember(member: UserInstance, group: GroupInstance): Boolean
    suspend fun demoteMember(member: UserInstance, group: GroupInstance): Boolean
    suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance>
    suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance>
}