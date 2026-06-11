package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.entity.authentication.TwoFAResponse
import com.minhtu.firesocialmedia.core.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.entity.settings.SessionItem
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.core.domain.usecases.settings.CreatePollUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.createpoll.CreatePollViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSettingsRepo : SettingsRepository {
    var createPollResult: Boolean = true
    override suspend fun createPoll(poll: PollObject, newsId: String, groupId: String) = createPollResult
    override suspend fun changePassword(user: UserInstance, newPassword: String) = ChangePasswordState()
    override suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String) = true
    override suspend fun generateSecretFor2FA() = ""
    override suspend fun copy(data: String) {}
    override suspend fun enableOTP(userId: String, secret: String, otpToVerify: String) = TwoFAResponse(success = false)
    override suspend fun verifyOTP(userId: String, otpToVerify: String) = TwoFAResponse(success = false)
    override suspend fun updateTwoFAEnabledFlagForUser(userId: String, twoFAEnabled: Boolean) = true
    override suspend fun disable2FA(userId: String) = TwoFAResponse(success = false)
    override suspend fun verifyBackupCode(userId: String, backupCode: String) = TwoFAResponse(success = false)
    override suspend fun updateVerify2FASuccess() {}
    override suspend fun get2FAVerifiedStatus() = false
    override suspend fun delete2FAStatusInLocal() {}
    override suspend fun fetchLoginHistoryList(userId: String) = emptyList<SessionItem>()
    override suspend fun deleteLoginSession(userId: String, sessionId: String) = true
    override suspend fun logoutSession(userId: String, sessionId: String) = true
    override fun observeSessionStatus(userId: String, sessionId: String, onLoggedOut: () -> Unit) {}
    override fun stopObserveSessionStatus() {}
    override suspend fun updateUserTimestamp(userId: String, fieldPath: String, value: Long) = true
    override suspend fun updateUserStringField(userId: String, fieldPath: String, value: String) = true
    override suspend fun updateUserAvatar(userId: String, imageUri: String) = true
    override suspend fun updateUserBackground(userId: String, imageUri: String) = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePollViewModelTest {

    @Test
    fun createPollSuccessSetsStateToTrue() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeSettingsRepo().apply { createPollResult = true }
        val vm = CreatePollViewModel(CreatePollUseCase(repo), dispatcher)
        vm.createPoll("posterId", "Name", "avatar", "My question?", listOf("A", "B"), false, "1 day", "g1")
        advanceUntilIdle()
        assertEquals(true, vm.createPollState.value)
    }

    @Test
    fun createPollFailureSetsStateToFalse() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeSettingsRepo().apply { createPollResult = false }
        val vm = CreatePollViewModel(CreatePollUseCase(repo), dispatcher)
        vm.createPoll("posterId", "Name", "avatar", "Question?", listOf("Yes", "No"), false, "1 week", "g2")
        advanceUntilIdle()
        assertEquals(false, vm.createPollState.value)
    }

    @Test
    fun resetCreatePollStateSetsToNull() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        vm.createPoll("p", "n", "a", "q", listOf("A", "B"), false, "1 day", "g1")
        advanceUntilIdle()
        vm.resetCreatePollState()
        assertNull(vm.createPollState.value)
    }

    @Test
    fun resetCreatePollDataClearsFormState() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        vm.setQuestion("Initial question")
        vm.setOption(0, "OptionA")
        vm.setAllowMultipleAnswers(true)
        vm.setSelectedDuration("3 days")
        vm.resetCreatePollData()
        assertEquals("", vm.question.value)
        assertEquals("", vm.options.value[0])
        assertEquals(false, vm.allowMultipleAnswers.value)
        assertEquals("1 day", vm.selectedDuration.value)
    }

    @Test
    fun addOptionIncreasesOptionsSize() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        val initialSize = vm.options.value.size
        vm.addOption()
        assertEquals(initialSize + 1, vm.options.value.size)
    }

    @Test
    fun removeOptionDecreasesOptionsSize() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        vm.addOption()
        val sizeAfterAdd = vm.options.value.size
        vm.removeOption(0)
        assertEquals(sizeAfterAdd - 1, vm.options.value.size)
    }

    @Test
    fun setOptionUpdatesValueAtIndex() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        vm.setOption(1, "Updated")
        assertEquals("Updated", vm.options.value[1])
    }

    @Test
    fun durationExpandedToggle() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        assertEquals(false, vm.durationExpanded.value)
        vm.setDurationExpanded(true)
        assertEquals(true, vm.durationExpanded.value)
    }

    @Test
    fun addOptionCappedAt10() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreatePollViewModel(CreatePollUseCase(FakeSettingsRepo()), dispatcher)
        repeat(15) { vm.addOption() }
        assertTrue(vm.options.value.size <= 10)
    }
}
