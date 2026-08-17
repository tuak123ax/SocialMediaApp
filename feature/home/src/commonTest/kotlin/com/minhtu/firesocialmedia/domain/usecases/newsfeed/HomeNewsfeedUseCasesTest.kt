package com.minhtu.firesocialmedia.domain.usecases.newsfeed

import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeNewsfeedUseCasesTest {

    private class FakeHomeDbRepository(
        val deleteAllDraftsResult: Boolean = true,
        val deleteDraftResult: Boolean = true,
        val saveNewResult: Boolean = true
    ) : HomeDbRepository {
        var savedNews: NewsInstance? = null
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
        override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean {
            savedNews = instance
            return saveNewResult
        }
        override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
        override suspend fun syncLikedPosts(currentUserId: String): Boolean = true
        override suspend fun clearLikedPosts() {}
        override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
        override suspend fun deleteAllDraftPosts(): Boolean = deleteAllDraftsResult
        override suspend fun deleteDraftPost(newId: String): Boolean = deleteDraftResult
        override suspend fun clearLocalFriends() {}
        override suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean = true
        override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = HashMap()
        override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean = false
    }

    private class FakeNewsRepository(
        val deletePollResult: Boolean = true,
        val poll: PollDTO? = null,
        val myVotes: List<Int> = emptyList(),
        val allVoters: Map<String, List<Int>> = emptyMap(),
        val submitVoteResult: Boolean = true,
        val updateResult: Boolean = true
    ) : NewsRepository {
        var updatedContent: String? = null
        override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = deletePollResult
        override suspend fun fetchPoll(pollId: String): PollDTO? = poll
        override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = myVotes
        override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = allVoters
        override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = submitVoteResult
        override suspend fun getNew(newId: String): NewsInstance? = null
        override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
        override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
        override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean {
            updatedContent = newContent
            return updateResult
        }
        override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = null
    }

    @Test
    fun `DeleteAllDraftPostsUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(deleteAllDraftsResult = true)
        assertTrue(DeleteAllDraftPostsUseCase(repo).invoke())
    }

    @Test
    fun `DeleteDraftPostUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(deleteDraftResult = false)
        assertFalse(DeleteDraftPostUseCase(repo).invoke("n1"))
    }

    @Test
    fun `DeletePollUseCase delegates to repository`() = runTest {
        val repo = FakeNewsRepository(deletePollResult = true)
        assertTrue(DeletePollUseCase(repo).invoke("n1", "p1", "g1"))
    }

    @Test
    fun `FetchPollUseCase maps DTO to domain`() = runTest {
        val dto = PollDTO(id = "p1", question = "Q?", options = listOf("A", "B"))
        val repo = FakeNewsRepository(poll = dto)
        val result = FetchPollUseCase(repo).invoke("p1")
        assertEquals("p1", result?.id)
        assertEquals("Q?", result?.question)
        assertEquals(listOf("A", "B"), result?.options)
    }

    @Test
    fun `FetchPollUseCase returns null when not found`() = runTest {
        val repo = FakeNewsRepository(poll = null)
        assertEquals(null, FetchPollUseCase(repo).invoke("missing"))
    }

    @Test
    fun `LoadAllVotersUseCase delegates to repository`() = runTest {
        val repo = FakeNewsRepository(allVoters = mapOf("0" to listOf(1, 2)))
        val result = LoadAllVotersUseCase(repo).invoke("p1")
        assertEquals(mapOf("0" to listOf(1, 2)), result)
    }

    @Test
    fun `LoadMyVotesUseCase delegates to repository`() = runTest {
        val repo = FakeNewsRepository(myVotes = listOf(1))
        val result = LoadMyVotesUseCase(repo).invoke("p1", "u1")
        assertEquals(listOf(1), result)
    }

    @Test
    fun `SaveNewToDatabaseUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(saveNewResult = true)
        val news = NewsInstance(id = "n1")
        assertTrue(SaveNewToDatabaseUseCase(repo).invoke(news))
        assertEquals("n1", repo.savedNews?.id)
    }

    @Test
    fun `SubmitVoteUseCase delegates to repository`() = runTest {
        val repo = FakeNewsRepository(submitVoteResult = true)
        val result = SubmitVoteUseCase(repo).invoke("p1", "u1", listOf(1), listOf(0))
        assertTrue(result)
    }

    @Test
    fun `UpdateNewsFromDatabaseUseCase delegates to repository`() = runTest {
        val repo = FakeNewsRepository(updateResult = true)
        val news = NewsInstance(id = "n1")
        val result = UpdateNewsFromDatabaseUseCase(repo).invoke("new content", "img.png", "", news)
        assertTrue(result)
        assertEquals("new content", repo.updatedContent)
    }
}
