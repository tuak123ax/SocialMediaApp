package com.minhtu.firesocialmedia.presentation.createpoll

import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.domain.usecases.settings.CreatePollUseCase
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
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

private class FakeCreatePollNewsRepo : GroupNewsRepository {
    var createPollResult: Boolean = true
    var lastPoll: PollDTO? = null
    var lastNewsId: String? = null
    var lastGroupId: String? = null

    override suspend fun getNew(newId: String): NewsInstance? = null
    override suspend fun deleteNewsFromDatabase(new: NewsInstance) = true
    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String) = true
    override suspend fun fetchPoll(pollId: String): PollDTO? = null
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
    override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>) = true
    override suspend fun createPoll(poll: PollDTO, newsId: String, groupId: String): Boolean {
        lastPoll = poll
        lastNewsId = newsId
        lastGroupId = groupId
        return createPollResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePollViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    private fun makeVm(repo: FakeCreatePollNewsRepo) = CreatePollViewModel(CreatePollUseCase(repo), dispatcher)

    @Test
    fun `createPoll success sets createPollState true and forwards data to use case`() = runTest(dispatcher) {
        val repo = FakeCreatePollNewsRepo().apply { createPollResult = true }
        val vm = makeVm(repo)

        vm.createPoll("p1", "Poster", "avatar", "Question?", listOf("A", "B"), false, "1 day", "g1")
        advanceUntilIdle()

        assertEquals(true, vm.createPollState.value)
        assertEquals("Question?", repo.lastPoll?.question)
        assertEquals("g1", repo.lastGroupId)
        assertEquals(repo.lastNewsId, repo.lastPoll?.id)
        assertTrue(repo.lastPoll?.id?.startsWith("Poll-") == true)
    }

    @Test
    fun `createPoll failure sets createPollState false`() = runTest(dispatcher) {
        val repo = FakeCreatePollNewsRepo().apply { createPollResult = false }
        val vm = makeVm(repo)

        vm.createPoll("p1", "Poster", "avatar", "Q", listOf("A", "B"), false, "1 day", "g1")
        advanceUntilIdle()

        assertEquals(false, vm.createPollState.value)
    }

    @Test
    fun `resetCreatePollState clears state to null`() = runTest(dispatcher) {
        val repo = FakeCreatePollNewsRepo().apply { createPollResult = true }
        val vm = makeVm(repo)
        vm.createPoll("p1", "Poster", "avatar", "Q", listOf("A", "B"), false, "1 day", "g1")
        advanceUntilIdle()

        vm.resetCreatePollState()

        assertNull(vm.createPollState.value)
    }

    @Test
    fun `resetCreatePollData resets all form fields to defaults`() {
        val vm = makeVm(FakeCreatePollNewsRepo())
        vm.setQuestion("Something")
        vm.addOption()
        vm.setAllowMultipleAnswers(true)
        vm.setSelectedDuration("1 week")
        vm.setDurationExpanded(true)

        vm.resetCreatePollData()

        assertEquals("", vm.question.value)
        assertEquals(listOf("", ""), vm.options.value)
        assertEquals(false, vm.allowMultipleAnswers.value)
        assertEquals("1 day", vm.selectedDuration.value)
        assertEquals(false, vm.durationExpanded.value)
    }

    @Test
    fun `setOption updates the option at given index`() {
        val vm = makeVm(FakeCreatePollNewsRepo())
        vm.setOption(0, "First")
        assertEquals(listOf("First", ""), vm.options.value)
    }

    @Test
    fun `addOption appends a new blank option`() {
        val vm = makeVm(FakeCreatePollNewsRepo())
        vm.addOption()
        assertEquals(3, vm.options.value.size)
    }

    @Test
    fun `addOption is capped at 10 options`() {
        val vm = makeVm(FakeCreatePollNewsRepo())
        repeat(15) { vm.addOption() }
        assertEquals(10, vm.options.value.size)
    }

    @Test
    fun `removeOption removes the option at given index`() {
        val vm = makeVm(FakeCreatePollNewsRepo())
        vm.addOption()
        vm.setOption(2, "Third")
        vm.removeOption(0)
        assertEquals(listOf("", "Third"), vm.options.value)
    }

    @Test
    fun `setDurationExpanded toggles state`() {
        val vm = makeVm(FakeCreatePollNewsRepo())
        assertEquals(false, vm.durationExpanded.value)
        vm.setDurationExpanded(true)
        assertEquals(true, vm.durationExpanded.value)
    }

    @Test
    fun `createPoll with Never duration results in null expiresAt`() = runTest(dispatcher) {
        val repo = FakeCreatePollNewsRepo().apply { createPollResult = true }
        val vm = makeVm(repo)

        vm.createPoll("p1", "Poster", "avatar", "Q", listOf("A", "B"), false, "Never", "g1")
        advanceUntilIdle()

        assertNull(repo.lastPoll?.expiresAt)
    }
}
