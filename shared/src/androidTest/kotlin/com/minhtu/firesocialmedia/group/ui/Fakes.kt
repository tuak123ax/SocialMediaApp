package com.minhtu.firesocialmedia.group.ui

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository

open class BaseFakeGroupRepository : GroupRepository {
	override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
	override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
	override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
	override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
	override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
	override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
	override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs()
	override suspend fun fetchNotificationState(userId: String, groupId: String) = false
	override suspend fun copyLink(copyData: String) {}
	override suspend fun inviteFriendToGroup(friend: UserInstance) {}
	override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
	override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
	override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
	override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
	override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}





