package com.minhtu.firesocialmedia.presentation.invitemember

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance as SharedNotificationInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeInviteGroupRepo : GroupRepository {
    var lastCopied: String? = null
    var groupInfo: GroupInstance = GroupInstance(id = "g1", name = "Group")
    override suspend fun copyLink(copyData: String) { lastCopied = copyData }
    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
    override suspend fun fetchGroupInfo(groupId: String) = groupInfo
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = false
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
    override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

private class FakeUserRepoForInvite : UserRepository {
    val users = mutableMapOf<String, UserDTO?>()
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class FakeInviteNotificationRepo : NotificationRepository {
    var saved: Pair<String, List<SharedNotificationInstance>>? = null
    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<SharedNotificationInstance>? = null
    override suspend fun saveNotificationToDatabase(id: String, instance: List<SharedNotificationInstance>) {
        saved = id to instance
    }
    override suspend fun deleteNotificationFromDatabase(id: String, notification: SharedNotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: SharedNotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

private fun makeVm(
    groupRepo: FakeInviteGroupRepo,
    userRepo: FakeUserRepoForInvite,
    notificationRepo: FakeInviteNotificationRepo,
    dispatcher: kotlinx.coroutines.CoroutineDispatcher
) = InviteMemberViewModel(
    CopyLinkUseCase(groupRepo),
    GetUserUseCase(userRepo),
    SaveNotificationToDatabaseUseCase(notificationRepo),
    FetchGroupInfoUseCase(groupRepo),
    dispatcher
)

@OptIn(ExperimentalCoroutinesApi::class)
class InviteMemberViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun copyLinkForwardsToRepository() = runTest(dispatcher) {
        val repo = FakeInviteGroupRepo()
        val vm = makeVm(repo, FakeUserRepoForInvite(), FakeInviteNotificationRepo(), dispatcher)
        vm.copyLink("https://link.test")
        advanceUntilIdle()
        assertEquals("https://link.test", repo.lastCopied)
    }

    @Test
    fun findUserByIdReturnsUserFromRepository() = runTest(dispatcher) {
        val userRepo = FakeUserRepoForInvite().apply { users["u1"] = UserDTO(uid = "u1", name = "Alice") }
        val vm = makeVm(FakeInviteGroupRepo(), userRepo, FakeInviteNotificationRepo(), dispatcher)
        val user = vm.findUserById("u1")
        assertNotNull(user)
        assertEquals("Alice", user.name)
    }

    @Test
    fun findUserByIdReturnsNullWhenNotFound() = runTest(dispatcher) {
        val vm = makeVm(FakeInviteGroupRepo(), FakeUserRepoForInvite(), FakeInviteNotificationRepo(), dispatcher)
        assertNull(vm.findUserById("unknown"))
    }

    @Test
    fun inviteFriendWithEmptyTokenAddsNotificationAndSaves() = runTest(dispatcher) {
        // A non-empty friend.token would route through createMessageForServer/sendMessageToServer,
        // which build an android.org.json.JSONObject not mocked under plain JUnit unit tests.
        // Using an empty token exercises the observable side effects (notification list mutation +
        // persistence) without touching that platform-specific code path.
        val notificationRepo = FakeInviteNotificationRepo()
        val vm = makeVm(FakeInviteGroupRepo(), FakeUserRepoForInvite(), notificationRepo, dispatcher)
        val current = UserInstance(uid = "me", name = "Me", image = "img")
        val friend = UserInstance(uid = "friend", token = "")
        val group = GroupInstance(id = "g1", name = "Group")
        vm.inviteFriendToGroup(current, friend, group)
        advanceUntilIdle()
        assertEquals(1, friend.notifications.size)
        assertEquals("friend", notificationRepo.saved?.first)
    }

    @Test
    fun fetchGroupInfoUpdatesState() = runTest(dispatcher) {
        val repo = FakeInviteGroupRepo().apply { groupInfo = GroupInstance(id = "gid", name = "TestGroup") }
        val vm = makeVm(repo, FakeUserRepoForInvite(), FakeInviteNotificationRepo(), dispatcher)
        vm.fetchGroupInfo("gid")
        advanceUntilIdle()
        assertEquals("gid", vm.fetchGroupInfoState.value?.id)
        assertEquals("TestGroup", vm.fetchGroupInfoState.value?.name)
    }

    @Test
    fun resetFetchGroupInfoStateSetsToNull() = runTest(dispatcher) {
        val vm = makeVm(FakeInviteGroupRepo(), FakeUserRepoForInvite(), FakeInviteNotificationRepo(), dispatcher)
        vm.fetchGroupInfo("gid")
        advanceUntilIdle()
        assertNotNull(vm.fetchGroupInfoState.value)
        vm.resetFetchGroupInfoState()
        assertNull(vm.fetchGroupInfoState.value)
    }
}
