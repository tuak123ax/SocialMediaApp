package com.minhtu.firesocialmedia.feature.friend

import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.feature.friend.presentation.friend.FriendViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeCommonDbRepository : CommonDbRepository {
	val savedFriends = mutableListOf<Pair<String, List<String>>>()
	val savedFriendRequests = mutableListOf<Pair<String, List<String>>>()

	override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
	override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean = true
	override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
	override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
	override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
	override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
	override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {}
	override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {}
	override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
	override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {}
	override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {}
	override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean = true
	override suspend fun saveFriend(id: String, value: ArrayList<String>) {
		savedFriends += id to value.toList()
	}

	override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
		savedFriendRequests += id to value.toList()
	}

	override suspend fun syncData(currentUserId: String): Boolean = true
	override suspend fun clearLikedPosts() {}
	override suspend fun clearComments() {}
	override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
	override suspend fun deleteAllDraftPosts(): Boolean = true
	override suspend fun deleteDraftPost(newId: String): Boolean = true
	override suspend fun clearLocalFriends() {}
	override suspend fun saveLoginActivityInfo(userId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {

	@Test
	fun `update methods replace friend and request state`() = runTest {
		val repo = FakeCommonDbRepository()
		val vm = FriendViewModel(
			saveFriendUseCase = SaveFriendUseCase(repo),
			saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
			ioDispatcher = StandardTestDispatcher(testScheduler)
		)

		vm.updateFriendRequests(listOf("a", "b"))
		vm.updateFriends(listOf("x"))

		assertEquals(listOf("a", "b"), vm.friendRequestsStatus.value)
		assertEquals(listOf("x"), vm.friendStatus.value)
	}

	@Test
	fun `acceptFriendRequest updates both users and persists friend data`() = runTest {
		val repo = FakeCommonDbRepository()
		val vm = FriendViewModel(
			saveFriendUseCase = SaveFriendUseCase(repo),
			saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
			ioDispatcher = StandardTestDispatcher(testScheduler)
		)

		val currentUser = UserInstance(uid = "current", friendRequests = arrayListOf("requester"))
		val requester = UserInstance(uid = "requester")

		vm.acceptFriendRequest(requester, currentUser)
		advanceUntilIdle()

		assertFalse(currentUser.friendRequests.contains("requester"))
		assertTrue(currentUser.friends.contains("requester"))
		assertTrue(requester.friends.contains("current"))
		assertEquals(listOf("current"), repo.savedFriends.last { it.first == "requester" }.second)
		assertEquals(emptyList(), repo.savedFriendRequests.last().second)
		assertEquals(listOf("requester"), vm.friendStatus.value)
	}

	@Test
	fun `rejectFriendRequest removes pending request and does not add friend`() = runTest {
		val repo = FakeCommonDbRepository()
		val vm = FriendViewModel(
			saveFriendUseCase = SaveFriendUseCase(repo),
			saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
			ioDispatcher = StandardTestDispatcher(testScheduler)
		)

		val currentUser = UserInstance(uid = "current", friendRequests = arrayListOf("requester"))
		val requester = UserInstance(uid = "requester")

		vm.rejectFriendRequest(requester, currentUser)
		advanceUntilIdle()

		assertFalse(currentUser.friendRequests.contains("requester"))
		assertTrue(currentUser.friends.isEmpty())
		assertEquals(emptyList(), vm.friendRequestsStatus.value)
		assertTrue(repo.savedFriends.isEmpty())
		assertEquals("current", repo.savedFriendRequests.single().first)
	}
}
