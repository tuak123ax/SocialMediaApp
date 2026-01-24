package com.minhtu.firesocialmedia.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllMembersInGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.GetGroupConfigsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingGroupRepository : GroupRepository {
    var savedGroup: GroupInstance? = null
    var savedUserId: String? = null
    var saveGroupResult: Boolean = true

    var lastGroupId: String? = null
    var groupInfo: GroupInstance = GroupInstance()

    var lastUpdateNotification: Triple<Boolean, String, String>? = null
    var updateNotificationResult: Boolean = true

    var lastJoin: Pair<UserInstance, GroupInstance>? = null
    var joinResult: Boolean = true
    var lastLeave: Pair<UserInstance, GroupInstance>? = null
    var leaveResult: Boolean = true
    var lastDelete: Pair<UserInstance, GroupInstance>? = null
    var lastPromote: Pair<UserInstance, GroupInstance>? = null
    var lastDemote: Pair<UserInstance, GroupInstance>? = null
    var lastRemoveMember: Pair<UserInstance, GroupInstance>? = null
    var deleteResult: Boolean = true

    var recommend: List<GroupInstance> = emptyList()
    var feature: List<GroupInstance> = emptyList()

    // Additional fields for extended tests
    var groupsForUser: Set<GroupInstance> = emptySet()
    var membersForGroup: HashMap<String, String> = hashMapOf()
    var configs: GroupConfigs = GroupConfigs()
    var notificationState: Boolean = false
    var lastCopied: String? = null
    var lastInvited: UserInstance? = null
    var lastSavedNews: Pair<NewsInstance, String>? = null

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String): Boolean {
        savedGroup = group
        savedUserId = userId
        return saveGroupResult
    }
    override suspend fun getAllGroups(userId: String): Set<GroupInstance> = groupsForUser
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance {
        lastGroupId = groupId
        return groupInfo
    }
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String): Boolean {
        lastSavedNews = instance to groupId
        return true
    }
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String): Boolean {
        lastUpdateNotification = Triple(newStatus, groupId, userId)
        return updateNotificationResult
    }
    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = membersForGroup
    override suspend fun getGroupConfigs(userId: String, groupId: String) = configs
    override suspend fun fetchNotificationState(userId: String, groupId: String): Boolean = notificationState
    override suspend fun copyLink(copyData: String) { lastCopied = copyData }
    override suspend fun inviteFriendToGroup(friend: UserInstance) { lastInvited = friend }
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance): Boolean {
        lastJoin = user to group
        return joinResult
    }
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance): Boolean {
        lastLeave = user to group
        return leaveResult
    }
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance): Boolean {
        lastDelete = user to group
        return deleteResult
    }
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance): Boolean {
        lastPromote = member to group
        return true
    }
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance): Boolean {
        lastDemote = member to group
        return true
    }
    override suspend fun removeMember(member: UserInstance, group: GroupInstance): Boolean {
        lastRemoveMember = member to group
        return true
    }
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> = recommend.take(limit)
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> = feature.take(limit)
}

class GroupUseCasesTest {

    @Test
    fun `CreateGroupUseCase delegates to repository`() = runTest {
        val repo = RecordingGroupRepository().apply { saveGroupResult = true }
        val useCase = CreateGroupUseCase(repo)
        val group = GroupInstance(id = "g1", name = "N")

        val result = useCase.invoke(group, "u1")

        assertTrue(result)
        assertEquals(group, repo.savedGroup)
        assertEquals("u1", repo.savedUserId)
    }

    @Test
    fun `FetchGroupInfoUseCase returns repository value`() = runTest {
        val repo = RecordingGroupRepository().apply { groupInfo = GroupInstance(id = "g2", name = "G") }
        val useCase = FetchGroupInfoUseCase(repo)

        val result = useCase.invoke("g2")

        assertEquals("g2", repo.lastGroupId)
        assertEquals("G", result.name)
    }

    @Test
    fun `UpdateNotificationStatusUseCase forwards arguments`() = runTest {
        val repo = RecordingGroupRepository().apply { updateNotificationResult = true }
        val useCase = UpdateNotificationStatusUseCase(repo)

        val ok = useCase.invoke(true, "g1", "u1")

        assertTrue(ok)
        assertEquals(Triple(true, "g1", "u1"), repo.lastUpdateNotification)
    }

    @Test
    fun `Join and Leave use cases forward arguments`() = runTest {
        val repo = RecordingGroupRepository()
        val join = JoinGroupUseCase(repo)
        val leave = LeaveGroupUseCase(repo)
        val delete = LeaveAndDeleteGroupUseCase(repo)
        val user = UserInstance(uid = "u9")
        val group = GroupInstance(id = "g9")

        assertTrue(join.invoke(user, group))
        assertEquals(user to group, repo.lastJoin)

        assertTrue(leave.invoke(user, group))
        assertEquals(user to group, repo.lastLeave)

        assertTrue(delete.invoke(user, group))
        assertEquals(user to group, repo.lastDelete)
    }

    @Test
    fun `Fetch recommend and feature groups enforce limit`() = runTest {
        val repo = RecordingGroupRepository().apply {
            recommend = (1..20).map { GroupInstance(id = "r$it") }
            feature = (1..5).map { GroupInstance(id = "f$it") }
        }
        val rec = FetchRecommendGroupsUseCase(repo)
        val feat = FetchFeatureGroupsUseCase(repo)

        val r = rec.invoke(10)
        val f = feat.invoke(10)

        assertEquals(10, r.size)
        assertEquals(5, f.size)
    }

    @Test
    fun `CopyLinkUseCase delegates to repository`() = runTest {
        val repo = RecordingGroupRepository()
        val useCase = CopyLinkUseCase(repo)
        useCase.invoke("https://example.com/g/1")
        assertEquals("https://example.com/g/1", repo.lastCopied)
    }

    @Test
    fun `InviteFriendToGroupUseCase delegates to repository`() = runTest {
        val repo = RecordingGroupRepository()
        val useCase = InviteFriendToGroupUseCase(repo)
        val friend = UserInstance(uid = "u2")
        useCase.invoke(friend)
        assertEquals(friend, repo.lastInvited)
    }

    @Test
    fun `GetAllGroupsUseCase returns groups for user`() = runTest {
        val repo = RecordingGroupRepository().apply {
            groupsForUser = setOf(GroupInstance(id = "g1"), GroupInstance(id = "g2"))
        }
        val useCase = GetAllGroupsUseCase(repo)
        val result = useCase.invoke("u1")
        assertEquals(2, result.size)
    }

    @Test
    fun `GetAllMembersInGroupUseCase returns members`() = runTest {
        val repo = RecordingGroupRepository().apply {
            membersForGroup = hashMapOf("u1" to "admin", "u2" to "member")
        }
        val useCase = GetAllMembersInGroupUseCase(repo)
        val result = useCase.invoke("g1")
        assertEquals(2, result.size)
        assertEquals("admin", result["u1"])
    }

    @Test
    fun `GetGroupConfigsUseCase returns configs`() = runTest {
        val cfg = GroupConfigs(notificationOn = true)
        val repo = RecordingGroupRepository().apply { configs = cfg }
        val useCase = GetGroupConfigsUseCase(repo)
        val result = useCase.invoke("u1", "g1")
        assertEquals(true, result.notificationOn)
    }

    @Test
    fun `FetchNotificationStateUseCase returns state`() = runTest {
        val repo = RecordingGroupRepository().apply { notificationState = true }
        val useCase = FetchNotificationStateUseCase(repo)
        val result = useCase.invoke("u1", "g1")
        assertEquals(true, result)
    }

    @Test
    fun `Promote Demote Remove member use cases forward`() = runTest {
        val repo = RecordingGroupRepository()
        val promote = PromoteMemberUseCase(repo)
        val demote = DemoteMemberUseCase(repo)
        val remove = RemoveMemberUseCase(repo)
        val user = UserInstance(uid = "u3")
        val group = GroupInstance(id = "g3")

        assertTrue(promote.invoke(user, group))
        assertEquals(user to group, repo.lastPromote)

        assertTrue(demote.invoke(user, group))
        assertEquals(user to group, repo.lastDemote)
        assertTrue(remove.invoke(user, group))
        assertEquals(user to group, repo.lastRemoveMember)
    }

    @Test
    fun `SaveNewToGroupUseCase forwards content`() = runTest {
        val repo = RecordingGroupRepository()
        val useCase = SaveNewToGroupUseCase(repo)
        val news = NewsInstance(id = "n1")
        val ok = useCase.invoke(news, "g5")
        assertTrue(ok)
        assertEquals("n1", repo.lastSavedNews?.first?.id)
        assertEquals("g5", repo.lastSavedNews?.second)
    }

    @Test
    fun `FindGroupByIdUseCase fetches by id`() = runTest {
        val repo = RecordingGroupRepository().apply { groupInfo = GroupInstance(id = "gid", name = "Nm") }
        val useCase = FindGroupByIdUseCase(repo)
        val g = useCase.invoke("gid")
        assertEquals("gid", g.id)
    }

    @Test
    fun `FindGroupInformationUseCase completes without error`() = runTest {
        val repo = RecordingGroupRepository()
        val useCase = FindGroupInformationUseCase(repo)
        // Should not throw
        useCase.invoke()
        assertTrue(true)
    }
}


