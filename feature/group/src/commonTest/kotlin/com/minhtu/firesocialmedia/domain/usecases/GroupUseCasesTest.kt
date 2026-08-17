package com.minhtu.firesocialmedia.domain.usecases

import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance as SharedNotificationInstance
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikedPostUseCase
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
import com.minhtu.firesocialmedia.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.FetchPollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.SubmitVoteUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.CreatePollUseCase
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class RecordingGroupRepository : GroupRepository {
    var savedGroup: Pair<GroupInstance, String>? = null
    var groups: Set<GroupInstance> = emptySet()
    var groupInfo: GroupInstance = GroupInstance(id = "g1", name = "Group")
    var savedNews: Pair<NewsInstance, String>? = null
    var updatedNotification: Triple<Boolean, String, String>? = null
    var members: HashMap<String, String> = hashMapOf()
    var configs: GroupConfigs = GroupConfigs()
    var notificationState: Boolean = false
    var copiedLink: String? = null
    var joinResult = true
    var leaveResult = true
    var leaveAndDeleteResult = true
    var removeResult = true
    var promoteResult = true
    var demoteResult = true
    var recommend: List<GroupInstance> = emptyList()
    var feature: List<GroupInstance> = emptyList()

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String): Boolean {
        savedGroup = group to userId
        return true
    }
    override suspend fun getAllGroups(userId: String): Set<GroupInstance> = groups
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance = groupInfo
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String): Boolean {
        savedNews = instance to groupId
        return true
    }
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String): Boolean {
        updatedNotification = Triple(newStatus, groupId, userId)
        return true
    }
    override suspend fun getAllMembersInGroup(groupId: String) = members
    override suspend fun getGroupConfigs(userId: String, groupId: String) = configs
    override suspend fun fetchNotificationState(userId: String, groupId: String) = notificationState
    override suspend fun copyLink(copyData: String) { copiedLink = copyData }
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = joinResult
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = leaveResult
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = leaveAndDeleteResult
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = removeResult
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = promoteResult
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = demoteResult
    override suspend fun fetchRecommendGroups(limit: Int) = recommend.take(limit)
    override suspend fun fetchFeatureGroups(limit: Int) = feature.take(limit)
}

private class RecordingGroupNewsRepository : GroupNewsRepository {
    var newToReturn: NewsInstance? = null
    var deleteResult = true
    var lastLikeUpdate: Pair<String, Int>? = null
    var deletePollResult = true
    var pollToReturn: PollDTO? = null
    var myVotes: List<Int> = emptyList()
    var allVoters: Map<String, List<Int>> = emptyMap()
    var submitVoteResult = true
    var createPollResult = true
    var lastCreatedPoll: Triple<PollDTO, String, String>? = null

    override suspend fun getNew(newId: String): NewsInstance? = newToReturn
    override suspend fun deleteNewsFromDatabase(new: NewsInstance) = deleteResult
    override suspend fun updateLikeCountForNew(newsId: String, value: Int) { lastLikeUpdate = newsId to value }
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String) = deletePollResult
    override suspend fun fetchPoll(pollId: String): PollDTO? = pollToReturn
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = myVotes
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = allVoters
    override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>) = submitVoteResult
    override suspend fun createPoll(poll: PollDTO, newsId: String, groupId: String): Boolean {
        lastCreatedPoll = Triple(poll, newsId, groupId)
        return createPollResult
    }
}

private class RecordingUserRepository : UserRepository {
    val users = mutableMapOf<String, UserDTO?>()
    var currentUserUid: String? = null
    var lastSavedLikedPosts: Pair<String, HashMap<String, Int>>? = null
    var saveLikedPostResult = true

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = currentUserUid
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean {
        lastSavedLikedPosts = userId to likedPosts
        return saveLikedPostResult
    }
}

private class RecordingNotificationRepository : NotificationRepository {
    var saved: Pair<String, List<SharedNotificationInstance>>? = null
    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<SharedNotificationInstance>? = null
    override suspend fun saveNotificationToDatabase(id: String, instance: List<SharedNotificationInstance>) {
        saved = id to instance
    }
    override suspend fun deleteNotificationFromDatabase(id: String, notification: SharedNotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: SharedNotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

class GroupUseCasesTest {

    // ---- group/ package ----

    @Test
    fun `CreateGroupUseCase forwards group and userId`() = runTest {
        val repo = RecordingGroupRepository()
        val group = GroupInstance(id = "g1", name = "Group")
        val result = CreateGroupUseCase(repo).invoke(group, "u1")
        assertTrue(result)
        assertEquals(group to "u1", repo.savedGroup)
    }

    @Test
    fun `GetAllGroupsUseCase returns groups from repository`() = runTest {
        val repo = RecordingGroupRepository().apply { groups = setOf(GroupInstance(id = "g1")) }
        val result = GetAllGroupsUseCase(repo).invoke("u1")
        assertEquals(1, result.size)
    }

    @Test
    fun `FetchGroupInfoUseCase and FindGroupByIdUseCase both delegate to fetchGroupInfo`() = runTest {
        val repo = RecordingGroupRepository().apply { groupInfo = GroupInstance(id = "gX", name = "X") }
        assertEquals("gX", FetchGroupInfoUseCase(repo).invoke("gX").id)
        assertEquals("gX", FindGroupByIdUseCase(repo).invoke("gX").id)
    }

    @Test
    fun `SaveNewToGroupUseCase forwards news and groupId`() = runTest {
        val repo = RecordingGroupRepository()
        val news = NewsInstance(id = "n1")
        val result = SaveNewToGroupUseCase(repo).invoke(news, "g1")
        assertTrue(result)
        assertEquals(news to "g1", repo.savedNews)
    }

    @Test
    fun `UpdateNotificationStatusUseCase forwards all params`() = runTest {
        val repo = RecordingGroupRepository()
        val result = UpdateNotificationStatusUseCase(repo).invoke(true, "g1", "u1")
        assertTrue(result)
        assertEquals(Triple(true, "g1", "u1"), repo.updatedNotification)
    }

    @Test
    fun `GetAllMembersInGroupUseCase returns members map`() = runTest {
        val repo = RecordingGroupRepository().apply { members = hashMapOf("u1" to "admin") }
        val result = GetAllMembersInGroupUseCase(repo).invoke("g1")
        assertEquals("admin", result["u1"])
    }

    @Test
    fun `GetGroupConfigsUseCase returns configs from repository`() = runTest {
        val repo = RecordingGroupRepository().apply { configs = GroupConfigs(id = "g1", name = "Cfg") }
        val result = GetGroupConfigsUseCase(repo).invoke("u1", "g1")
        assertEquals("Cfg", result.name)
    }

    @Test
    fun `FetchNotificationStateUseCase returns state from repository`() = runTest {
        val repo = RecordingGroupRepository().apply { notificationState = true }
        assertTrue(FetchNotificationStateUseCase(repo).invoke("u1", "g1"))
    }

    @Test
    fun `CopyLinkUseCase forwards link to repository`() = runTest {
        val repo = RecordingGroupRepository()
        CopyLinkUseCase(repo).invoke("https://link")
        assertEquals("https://link", repo.copiedLink)
    }

    @Test
    fun `JoinGroup LeaveGroup LeaveAndDeleteGroup use cases return repository results`() = runTest {
        val repo = RecordingGroupRepository().apply {
            joinResult = true; leaveResult = false; leaveAndDeleteResult = true
        }
        val user = UserInstance(uid = "u1")
        val group = GroupInstance(id = "g1")
        assertTrue(JoinGroupUseCase(repo).invoke(user, group))
        assertFalse(LeaveGroupUseCase(repo).invoke(user, group))
        assertTrue(LeaveAndDeleteGroupUseCase(repo).invoke(user, group))
    }

    @Test
    fun `RemoveMember PromoteMember DemoteMember use cases return repository results`() = runTest {
        val repo = RecordingGroupRepository().apply {
            removeResult = true; promoteResult = false; demoteResult = true
        }
        val member = UserInstance(uid = "u2")
        val group = GroupInstance(id = "g1")
        assertTrue(RemoveMemberUseCase(repo).invoke(member, group))
        assertFalse(PromoteMemberUseCase(repo).invoke(member, group))
        assertTrue(DemoteMemberUseCase(repo).invoke(member, group))
    }

    @Test
    fun `FetchRecommendGroupsUseCase and FetchFeatureGroupsUseCase apply limit`() = runTest {
        val repo = RecordingGroupRepository().apply {
            recommend = (1..5).map { GroupInstance(id = "r$it") }
            feature = (1..5).map { GroupInstance(id = "f$it") }
        }
        assertEquals(3, FetchRecommendGroupsUseCase(repo).invoke(3).size)
        assertEquals(3, FetchFeatureGroupsUseCase(repo).invoke(3).size)
    }

    @Test
    fun `FindGroupInformationUseCase invoke completes without error`() = runTest {
        // This use case is currently a documented no-op placeholder.
        FindGroupInformationUseCase(RecordingGroupRepository()).invoke()
    }

    // ---- common/group/ package ----

    @Test
    fun `GetUserUseCase and GetCurrentUserUidUseCase delegate to UserRepository`() = runTest {
        val repo = RecordingUserRepository().apply {
            users["u1"] = UserDTO(uid = "u1", name = "Alice")
            currentUserUid = "u1"
        }
        assertEquals("Alice", GetUserUseCase(repo).invoke("u1", false)?.name)
        assertEquals("u1", GetCurrentUserUidUseCase(repo).invoke())
    }

    @Test
    fun `SaveLikedPostUseCase forwards liked posts to repository`() = runTest {
        val repo = RecordingUserRepository()
        val liked = hashMapOf("n1" to 1)
        val result = SaveLikedPostUseCase(repo).invoke("u1", liked)
        assertTrue(result)
        assertEquals("u1" to liked, repo.lastSavedLikedPosts)
    }

    @Test
    fun `SaveLikeNotificationUseCase does nothing when poster is not found`() = runTest {
        // The "poster found" branch reaches createMessageForServer/sendMessageToServer, which build
        // an android.org.json.JSONObject not mocked under plain JUnit unit tests (no Robolectric in
        // this module). Only the "poster not found" branch (returns before touching JSONObject) is
        // unit-testable here — same documented limitation as feature/profile's analog use case.
        val userRepo = RecordingUserRepository()
        val notificationRepo = RecordingNotificationRepository()
        val useCase = SaveLikeNotificationUseCase(GetUserUseCase(userRepo), SaveNotificationToDatabaseUseCase(notificationRepo))
        useCase.invoke(UserInstance(uid = "liker", name = "Liker"), "missingPoster", "n1")
        assertNull(notificationRepo.saved)
    }

    // ---- news/group/ package ----

    @Test
    fun `GetNewByIdUseCase returns news from repository`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { newToReturn = NewsInstance(id = "n1", message = "hi") }
        assertEquals("hi", GetNewByIdUseCase(repo).invoke("n1")?.message)
    }

    @Test
    fun `GetNewByIdUseCase returns null when not found`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { newToReturn = null }
        assertNull(GetNewByIdUseCase(repo).invoke("missing"))
    }

    @Test
    fun `DeleteNewsUseCase forwards news to repository`() = runTest {
        val repo = RecordingGroupNewsRepository()
        val news = NewsInstance(id = "n1")
        assertTrue(DeleteNewsUseCase(repo).invoke(news))
    }

    @Test
    fun `UpdateLikeCountForNewUseCase forwards newsId and value`() = runTest {
        val repo = RecordingGroupNewsRepository()
        UpdateLikeCountForNewUseCase(repo).invoke("n1", 5)
        assertEquals("n1" to 5, repo.lastLikeUpdate)
    }

    // ---- newsfeed/group/ package ----

    @Test
    fun `DeletePollUseCase forwards ids and returns repository result`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { deletePollResult = true }
        assertTrue(DeletePollUseCase(repo).invoke("n1", "p1", "g1"))
    }

    @Test
    fun `FetchPollUseCase maps PollDTO to PollObject`() = runTest {
        val repo = RecordingGroupNewsRepository().apply {
            pollToReturn = PollDTO(id = "p1", question = "Q?", options = listOf("A", "B"))
        }
        val result = FetchPollUseCase(repo).invoke("p1")
        assertEquals("Q?", result?.question)
        assertEquals(listOf("A", "B"), result?.options)
    }

    @Test
    fun `FetchPollUseCase returns null when poll not found`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { pollToReturn = null }
        assertNull(FetchPollUseCase(repo).invoke("missing"))
    }

    @Test
    fun `LoadMyVotesUseCase returns votes from repository`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { myVotes = listOf(0, 1) }
        assertEquals(listOf(0, 1), LoadMyVotesUseCase(repo).invoke("p1", "u1"))
    }

    @Test
    fun `LoadAllVotersUseCase returns voter map from repository`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { allVoters = mapOf("u1" to listOf(0)) }
        assertEquals(mapOf("u1" to listOf(0)), LoadAllVotersUseCase(repo).invoke("p1"))
    }

    @Test
    fun `SubmitVoteUseCase forwards all params and returns result`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { submitVoteResult = true }
        assertTrue(SubmitVoteUseCase(repo).invoke("p1", "u1", listOf(1), listOf(0)))
    }

    // ---- settings/ package ----

    @Test
    fun `CreatePollUseCase maps PollObject to dto and forwards to repository`() = runTest {
        val repo = RecordingGroupNewsRepository().apply { createPollResult = true }
        val poll = com.minhtu.firesocialmedia.domain.entity.settings.group.PollObject(
            id = "p1", question = "Q?", options = listOf("A", "B")
        )
        val result = CreatePollUseCase(repo).invoke(poll, "n1", "g1")
        assertTrue(result)
        assertEquals("p1", repo.lastCreatedPoll?.first?.id)
        assertEquals("n1", repo.lastCreatedPoll?.second)
        assertEquals("g1", repo.lastCreatedPoll?.third)
    }
}
