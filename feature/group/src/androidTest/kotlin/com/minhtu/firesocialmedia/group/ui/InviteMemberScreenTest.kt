package com.minhtu.firesocialmedia.group.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.presentation.invitemember.InviteMember
import com.minhtu.firesocialmedia.presentation.invitemember.InviteMemberViewModel
import com.seiko.imageloader.LocalImageLoader
import org.junit.Rule
import org.junit.Test

private class UserRepoForInvite : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = UserDTO(uid = userId, name = "U")
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class RepoForInvite : BaseFakeGroupRepository() {
    override suspend fun fetchGroupInfo(groupId: String) = GroupInstance(id = groupId, name = "G")
}

private class NotificationRepoForInvite : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<NotificationInstance>? = null
    override suspend fun saveNotificationToDatabase(id: String, instance: List<NotificationInstance>) {}
    override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: NotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

class InviteMemberScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_search_bar() {
        val repo = RepoForInvite()
        val vm = InviteMemberViewModel(
            copyLinkUseCase = CopyLinkUseCase(repo),
            getUserUseCase = GetUserUseCase(UserRepoForInvite()),
            saveNotificationToDatabaseUseCase = SaveNotificationToDatabaseUseCase(NotificationRepoForInvite()),
            fetchGroupInfoUseCase = FetchGroupInfoUseCase(repo)
        )
        val currentUser = UserInstance(uid = "u1")

        composeRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader()) {
                InviteMember.InviteMemberScreen(
                    groupId = "g1",
                    currentUser = currentUser,
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                    localImageLoaderValue = LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader(),
                    inviteMemberViewModel = vm,
                    onNavigateBack = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_SEARCH_BAR).assertIsDisplayed()
    }
}


