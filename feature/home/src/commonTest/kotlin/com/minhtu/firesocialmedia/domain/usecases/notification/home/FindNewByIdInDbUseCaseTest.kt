package com.minhtu.firesocialmedia.domain.usecases.notification.home

import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FindNewByIdInDbUseCaseTest {

    private class FakeNewsRepository(val newById: Map<String, NewsInstance?> = emptyMap()) : NewsRepository {
        override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
        override suspend fun fetchPoll(pollId: String): PollDTO? = null
        override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
        override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
        override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
        override suspend fun getNew(newId: String): NewsInstance? = newById[newId]
        override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
        override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
        override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean = true
        override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = null
    }

    @Test
    fun `invoke returns the found news`() = runTest {
        val news = NewsInstance(id = "n1", message = "hi")
        val useCase = FindNewByIdInDbUseCase(FakeNewsRepository(mapOf("n1" to news)))
        val result = useCase.invoke("n1")
        assertEquals("hi", result.message)
    }

    @Test
    fun `invoke returns default NewsInstance when not found`() = runTest {
        val useCase = FindNewByIdInDbUseCase(FakeNewsRepository())
        val result = useCase.invoke("missing")
        assertEquals("", result.id)
    }
}
