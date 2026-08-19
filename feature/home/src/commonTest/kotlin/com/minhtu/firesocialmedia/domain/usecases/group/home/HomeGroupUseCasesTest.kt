package com.minhtu.firesocialmedia.domain.usecases.group.home

import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeGroupUseCasesTest {

    private class FakeHomeDbRepository(
        val membersInGroup: HashMap<String, String> = HashMap(),
        val notificationOn: Boolean = false,
        val saveNewToGroupResult: Boolean = true
    ) : HomeDbRepository {
        var savedGroupId: String? = null
        var savedInstance: NewsInstance? = null
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
        override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean = true
        override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
        override suspend fun syncLikedPosts(currentUserId: String): Boolean = true
        override suspend fun clearLikedPosts() {}
        override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
        override suspend fun deleteAllDraftPosts(): Boolean = true
        override suspend fun deleteDraftPost(newId: String): Boolean = true
        override suspend fun clearLocalFriends() {}
        override suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean {
            savedGroupId = groupId
            savedInstance = instance
            return saveNewToGroupResult
        }
        override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = membersInGroup
        override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean = notificationOn
    }

    @Test
    fun `GetAllMembersInGroupUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(membersInGroup = hashMapOf("u1" to "Alice"))
        val result = GetAllMembersInGroupUseCase(repo).invoke("group1")
        assertEquals(hashMapOf("u1" to "Alice"), result)
    }

    @Test
    fun `GetGroupConfigsUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(notificationOn = true)
        val result = GetGroupConfigsUseCase(repo).invoke("u1", "group1")
        assertTrue(result)
    }

    @Test
    fun `GetGroupConfigsUseCase returns false when notification off`() = runTest {
        val repo = FakeHomeDbRepository(notificationOn = false)
        val result = GetGroupConfigsUseCase(repo).invoke("u1", "group1")
        assertFalse(result)
    }

    @Test
    fun `SaveNewToGroupUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(saveNewToGroupResult = true)
        val news = NewsInstance(id = "n1")
        val result = SaveNewToGroupUseCase(repo).invoke(news, "group1")
        assertTrue(result)
        assertEquals("group1", repo.savedGroupId)
        assertEquals("n1", repo.savedInstance?.id)
    }
}
