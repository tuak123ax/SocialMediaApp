package com.minhtu.firesocialmedia.presentation.groupdetails

import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.FetchPollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.SubmitVoteUseCase
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
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
import kotlin.test.assertTrue

private class FakePollNewsRepo : GroupNewsRepository {
    var pollToReturn: PollDTO? = PollDTO(id = "p1", question = "Q?", options = listOf("A", "B"))
    var myVotes: List<Int> = emptyList()
    var allVoters: Map<String, List<Int>> = emptyMap()
    var submitVoteResult: Boolean = true
    var lastSubmit: Triple<List<Int>, List<Int>, Boolean>? = null // selected, previous, result

    override suspend fun getNew(newId: String) = null
    override suspend fun deleteNewsFromDatabase(new: com.minhtu.firesocialmedia.group.entity.news.NewsInstance) = true
    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String) = true
    override suspend fun fetchPoll(pollId: String): PollDTO? = pollToReturn
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = myVotes
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = allVoters
    override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean {
        lastSubmit = Triple(selectedIndices, previousIndices, submitVoteResult)
        return submitVoteResult
    }
    override suspend fun createPoll(poll: PollDTO, newsId: String, groupId: String) = true
}

private class FakePollUserRepo : UserRepository {
    val users = mutableMapOf<String, UserDTO?>()
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class PollViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    private fun makeVm(
        newsRepo: FakePollNewsRepo,
        userRepo: FakePollUserRepo = FakePollUserRepo(),
        includeVoters: Boolean = true
    ) = PollViewModel(
        FetchPollUseCase(newsRepo),
        LoadMyVotesUseCase(newsRepo),
        SubmitVoteUseCase(newsRepo),
        if (includeVoters) LoadAllVotersUseCase(newsRepo) else null,
        if (includeVoters) GetUserUseCase(userRepo) else null,
        dispatcher
    )

    @Test
    fun `loadPoll fetches poll and votes`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { myVotes = listOf(1) }
        val vm = makeVm(newsRepo)

        vm.loadPoll("p1", "u1")
        advanceUntilIdle()

        assertEquals("Q?", vm.polls.value["p1"]?.question)
        assertEquals(listOf(1), vm.myVotes.value["p1"])
    }

    @Test
    fun `loadPoll with blank pollId does nothing`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo()
        val vm = makeVm(newsRepo)

        vm.loadPoll("", "u1")
        advanceUntilIdle()

        assertTrue(vm.polls.value.isEmpty())
    }

    @Test
    fun `loadPoll skips refetch when already loaded`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { pollToReturn = PollDTO(id = "p1", question = "First") }
        val vm = makeVm(newsRepo)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()

        newsRepo.pollToReturn = PollDTO(id = "p1", question = "Second")
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()

        assertEquals("First", vm.polls.value["p1"]?.question)
    }

    @Test
    fun `submitVote success updates myVotes and submitState and refreshes poll`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { submitVoteResult = true }
        val vm = makeVm(newsRepo)

        vm.submitVote("p1", "u1", listOf(0))
        advanceUntilIdle()

        assertEquals(listOf(0), vm.myVotes.value["p1"])
        assertEquals(true, vm.submitState.value["p1"])
        assertEquals("Q?", vm.polls.value["p1"]?.question)
    }

    @Test
    fun `submitVote failure sets submitState to false without updating myVotes`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { submitVoteResult = false }
        val vm = makeVm(newsRepo)

        vm.submitVote("p1", "u1", listOf(0))
        advanceUntilIdle()

        assertEquals(false, vm.submitState.value["p1"])
        assertTrue(vm.myVotes.value["p1"] == null)
    }

    @Test
    fun `resetSubmitState clears state to null`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { submitVoteResult = true }
        val vm = makeVm(newsRepo)
        vm.submitVote("p1", "u1", listOf(0))
        advanceUntilIdle()

        vm.resetSubmitState("p1")

        assertNull(vm.submitState.value["p1"])
    }

    @Test
    fun `clearMyVote clears display cache only`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { myVotes = listOf(1) }
        val vm = makeVm(newsRepo)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()

        vm.clearMyVote("p1")
        assertEquals(emptyList(), vm.myVotes.value["p1"])

        // Submitting afterwards should still use the previously-persisted server vote (1) as previous.
        vm.submitVote("p1", "u1", listOf(0))
        advanceUntilIdle()
        assertEquals(listOf(1), newsRepo.lastSubmit?.second)
    }

    @Test
    fun `loadAllVoters resolves users and populates allVoters`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { allVoters = mapOf("u2" to listOf(0)) }
        val userRepo = FakePollUserRepo().apply { users["u2"] = UserDTO(uid = "u2", name = "Voter") }
        val vm = makeVm(newsRepo, userRepo)

        vm.loadAllVoters("p1")
        advanceUntilIdle()

        val voters = vm.allVoters.value["p1"]
        assertEquals(1, voters?.size)
        assertEquals("Voter", voters?.first()?.first?.name)
    }

    @Test
    fun `loadAllVoters does nothing when loadAllVotersUseCase is null`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { allVoters = mapOf("u2" to listOf(0)) }
        val vm = makeVm(newsRepo, includeVoters = false)

        vm.loadAllVoters("p1")
        advanceUntilIdle()

        assertTrue(vm.allVoters.value.isEmpty())
    }

    @Test
    fun `refreshAllVoters reloads voters even if already loaded`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { allVoters = mapOf("u2" to listOf(0)) }
        val userRepo = FakePollUserRepo().apply { users["u2"] = UserDTO(uid = "u2", name = "Voter") }
        val vm = makeVm(newsRepo, userRepo)
        vm.loadAllVoters("p1")
        advanceUntilIdle()

        newsRepo.allVoters = mapOf("u2" to listOf(0), "u3" to listOf(1))
        userRepo.users["u3"] = UserDTO(uid = "u3", name = "Voter2")
        vm.refreshAllVoters("p1")
        advanceUntilIdle()

        assertEquals(2, vm.allVoters.value["p1"]?.size)
    }

    @Test
    fun `refreshPoll refetches poll data`() = runTest(dispatcher) {
        val newsRepo = FakePollNewsRepo().apply { pollToReturn = PollDTO(id = "p1", question = "Original") }
        val vm = makeVm(newsRepo)
        vm.loadPoll("p1", "u1")
        advanceUntilIdle()

        newsRepo.pollToReturn = PollDTO(id = "p1", question = "Updated")
        vm.refreshPoll("p1", "u1")
        advanceUntilIdle()

        assertEquals("Updated", vm.polls.value["p1"]?.question)
    }
}
