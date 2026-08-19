package com.minhtu.firesocialmedia.domain.usecases.sync

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncUseCasesTest {

    private class SyncFakeHomeDbRepository(
        val syncLikedPostsResult: Boolean = true,
        val offlineNews: List<NewsInstance> = emptyList()
    ) : HomeDbRepository {
        var syncedUserId: String? = null
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
        override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean = true
        override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
        override suspend fun syncLikedPosts(currentUserId: String): Boolean {
            syncedUserId = currentUserId
            return syncLikedPostsResult
        }
        override suspend fun clearLikedPosts() {}
        override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = offlineNews
        override suspend fun deleteAllDraftPosts(): Boolean = true
        override suspend fun deleteDraftPost(newId: String): Boolean = true
        override suspend fun clearLocalFriends() {}
        override suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean = true
        override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = HashMap()
        override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean = false
    }

    private class SyncFakeHomeCommentDbRepository(
        val syncCommentsResult: Boolean = true
    ) : HomeCommentDbRepository {
        override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
        override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
        override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
        override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
        override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {}
        override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {}
        override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {}
        override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {}
        override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean = true
        override suspend fun syncComments(): Boolean = syncCommentsResult
        override suspend fun clearComments() {}
    }

    @Test
    fun `SyncDataUseCase returns true when both syncs succeed`() = runTest {
        val homeRepo = SyncFakeHomeDbRepository(syncLikedPostsResult = true)
        val commentRepo = SyncFakeHomeCommentDbRepository(syncCommentsResult = true)
        val result = SyncDataUseCase(homeRepo, commentRepo).invoke("user1")
        assertTrue(result)
        assertEquals("user1", homeRepo.syncedUserId)
    }

    @Test
    fun `SyncDataUseCase returns false when liked posts sync fails`() = runTest {
        val homeRepo = SyncFakeHomeDbRepository(syncLikedPostsResult = false)
        val commentRepo = SyncFakeHomeCommentDbRepository(syncCommentsResult = true)
        val result = SyncDataUseCase(homeRepo, commentRepo).invoke("user1")
        assertFalse(result)
    }

    @Test
    fun `SyncDataUseCase returns false when comment sync fails`() = runTest {
        val homeRepo = SyncFakeHomeDbRepository(syncLikedPostsResult = true)
        val commentRepo = SyncFakeHomeCommentDbRepository(syncCommentsResult = false)
        val result = SyncDataUseCase(homeRepo, commentRepo).invoke("user1")
        assertFalse(result)
    }

    @Test
    fun `SyncDataUseCase returns false when both syncs fail`() = runTest {
        val homeRepo = SyncFakeHomeDbRepository(syncLikedPostsResult = false)
        val commentRepo = SyncFakeHomeCommentDbRepository(syncCommentsResult = false)
        val result = SyncDataUseCase(homeRepo, commentRepo).invoke("user1")
        assertFalse(result)
    }

    @Test
    fun `LoadNewsPostedWhenOfflineUseCase delegates to repository`() = runTest {
        val news = listOf(NewsInstance(id = "n1"), NewsInstance(id = "n2"))
        val homeRepo = SyncFakeHomeDbRepository(offlineNews = news)
        val result = LoadNewsPostedWhenOfflineUseCase(homeRepo).invoke()
        assertEquals(news, result)
    }

    @Test
    fun `LoadNewsPostedWhenOfflineUseCase returns empty list when none pending`() = runTest {
        val homeRepo = SyncFakeHomeDbRepository(offlineNews = emptyList())
        val result = LoadNewsPostedWhenOfflineUseCase(homeRepo).invoke()
        assertTrue(result.isEmpty())
    }
}
