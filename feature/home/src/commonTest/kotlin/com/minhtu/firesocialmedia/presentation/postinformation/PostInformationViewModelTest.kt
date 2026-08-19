package com.minhtu.firesocialmedia.presentation.postinformation

import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PostInformationViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeNewsRepository(
        val newsById: Map<String, NewsInstance?> = emptyMap()
    ) : NewsRepository {
        override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
        override suspend fun fetchPoll(pollId: String): PollDTO? = null
        override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
        override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
        override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
        override suspend fun getNew(newId: String): NewsInstance? = newsById[newId]
        override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
        override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
        override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean = true
        override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = null
    }

    @Test
    fun `requestFindNewById sets news when found`() = runTest {
        val news = NewsInstance(id = "n1", message = "hi")
        val vm = PostInformationViewModel(FakeNewsRepository(mapOf("n1" to news)), StandardTestDispatcher(testScheduler))
        vm.requestFindNewById("n1")
        assertEquals("hi", vm.newFromDeepLink.value?.message)
    }

    @Test
    fun `requestFindNewById defaults to empty NewsInstance when not found`() = runTest {
        val vm = PostInformationViewModel(FakeNewsRepository(), StandardTestDispatcher(testScheduler))
        vm.requestFindNewById("missing")
        assertEquals("", vm.newFromDeepLink.value?.id)
    }

    @Test
    fun `updateShareNew and resetShareNew toggle state`() = runTest {
        val vm = PostInformationViewModel(FakeNewsRepository(), StandardTestDispatcher(testScheduler))
        val news = NewsInstance(id = "n1")
        vm.updateShareNew(news)
        assertEquals("n1", vm.sharedNew.value?.id)
        vm.resetShareNew()
        assertNull(vm.sharedNew.value)
    }

    @Test
    fun `getSharedNew loads news asynchronously`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val news = NewsInstance(id = "n2", message = "shared")
        val vm = PostInformationViewModel(FakeNewsRepository(mapOf("n2" to news)), StandardTestDispatcher(testScheduler))
        vm.getSharedNew("n2")
        advanceUntilIdle()
        assertEquals("shared", vm.sharedNew.value?.message)
    }

    @Test
    fun `getSharedNew defaults to empty NewsInstance when not found`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = PostInformationViewModel(FakeNewsRepository(), StandardTestDispatcher(testScheduler))
        vm.getSharedNew("missing")
        advanceUntilIdle()
        assertEquals("", vm.sharedNew.value?.id)
    }
}
