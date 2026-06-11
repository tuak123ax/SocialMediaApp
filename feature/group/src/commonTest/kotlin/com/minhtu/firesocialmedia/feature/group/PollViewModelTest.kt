package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.FetchPollUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.SubmitVoteUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.groupdetails.PollViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class FakePollNewsRepo : NewsRepository {
    var pollToReturn: PollObject? = PollObject(id = "p1", question = "Q?", options = listOf("A", "B"))
    var myVotesResult: List<Int> = emptyList()
    var submitResult: Boolean = true
    var allVotersResult: Map<String, List<Int>> = emptyMap()

    override suspend fun getNew(newId: String): NewsInstance? = null
    override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?) = null
    override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String) = true
    override suspend fun updateNewsFromDatabase(
        newContent: String, newImage: String, newVideo: String, new: NewsInstance
    ) = true
    override suspend fun fetchPoll(pollId: String): PollObject? = pollToReturn
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = myVotesResult
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = allVotersResult
    override suspend fun submitVote(
        pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>
    ): Boolean = submitResult
}

private class FakePollUserRepo : UserRepository {
    val users = mutableMapOf<String, UserInstance?>()
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PollViewModelTest {

    private fun makeVm(
        newsRepo: FakePollNewsRepo,
        userRepo: FakePollUserRepo? = null,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher
    ) = PollViewModel(
        FetchPollUseCase(newsRepo),
        LoadMyVotesUseCase(newsRepo),
        SubmitVoteUseCase(newsRepo),
        if (userRepo != null) LoadAllVotersUseCase(newsRepo) else null,
        if (userRepo != null) GetUserUseCase(userRepo) else null,
        dispatcher
    )

    @Test
    fun loadPollFetchesPollAndVotes() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo()
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()
        assertNotNull(vm.polls.value["p1"])
        assertEquals("Q?", vm.polls.value["p1"]?.question)
        assert(vm.myVotes.value.containsKey("p1"))
    }

    @Test
    fun loadPollSkipsIfAlreadyCached() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo().apply { myVotesResult = listOf(0) }
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()
        newsRepo.pollToReturn = PollObject(id = "p1", question = "CHANGED")
        vm.loadPoll("p1", "u1") // should be skipped (already cached)
        advanceUntilIdle()
        assertEquals("Q?", vm.polls.value["p1"]?.question)
    }

    @Test
    fun submitVoteSuccessUpdatesCacheAndRefreshes() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo().apply { submitResult = true }
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.submitVote("p1", "u1", listOf(0))
        advanceUntilIdle()
        assertEquals(true, vm.submitState.value["p1"])
        assertEquals(listOf(0), vm.myVotes.value["p1"])
    }

    @Test
    fun submitVoteFailureSetsFalse() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo().apply { submitResult = false }
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.submitVote("p1", "u1", listOf(1))
        advanceUntilIdle()
        assertEquals(false, vm.submitState.value["p1"])
    }

    @Test
    fun resetSubmitStateSetsToNull() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo()
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.submitVote("p1", "u1", listOf(0))
        advanceUntilIdle()
        vm.resetSubmitState("p1")
        assertNull(vm.submitState.value["p1"])
    }

    @Test
    fun clearMyVoteClearsDisplayCacheOnly() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo().apply { myVotesResult = listOf(0) }
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()
        assertEquals(listOf(0), vm.myVotes.value["p1"])
        vm.clearMyVote("p1")
        assertEquals(emptyList<Int>(), vm.myVotes.value["p1"])
    }

    @Test
    fun loadAllVotersResolvesUsersFromRepo() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo().apply {
            allVotersResult = mapOf("u1" to listOf(0), "u2" to listOf(1))
        }
        val userRepo = FakePollUserRepo().apply {
            users["u1"] = UserInstance(uid = "u1", name = "Alice")
            users["u2"] = UserInstance(uid = "u2", name = "Bob")
        }
        val vm = makeVm(newsRepo, userRepo, dispatcher)
        vm.loadAllVoters("p1")
        advanceUntilIdle()
        val voters = vm.allVoters.value["p1"]
        assertNotNull(voters)
        assertEquals(2, voters?.size)
    }

    @Test
    fun refreshAllVotersReloadsVoterList() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo().apply {
            allVotersResult = mapOf("u1" to listOf(0))
        }
        val userRepo = FakePollUserRepo()
        val vm = makeVm(newsRepo, userRepo, dispatcher)
        vm.loadAllVoters("p1")
        advanceUntilIdle()
        assertEquals(1, vm.allVoters.value["p1"]?.size)
        newsRepo.allVotersResult = mapOf("u1" to listOf(0), "u2" to listOf(1))
        vm.refreshAllVoters("p1")
        advanceUntilIdle()
        assertEquals(2, vm.allVoters.value["p1"]?.size)
    }

    @Test
    fun refreshPollUpdatesCache() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val newsRepo = FakePollNewsRepo()
        val vm = makeVm(newsRepo, dispatcher = dispatcher)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()
        newsRepo.pollToReturn = PollObject(id = "p1", question = "Refreshed?")
        vm.refreshPoll("p1", "u1")
        advanceUntilIdle()
        assertEquals("Refreshed?", vm.polls.value["p1"]?.question)
    }
}

