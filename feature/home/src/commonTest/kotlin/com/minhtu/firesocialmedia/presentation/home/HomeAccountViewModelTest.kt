package com.minhtu.firesocialmedia.presentation.home

import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeAccountViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeHomeCryptoService : HomeCryptoService {
        var cleared = false
        override suspend fun clearAccount() {
            cleared = true
        }
        override suspend fun saveCurrentUserInfo(user: UserDTO) {}
    }

    @Test
    fun `clearAccountInStorage delegates to crypto service`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val cryptoService = FakeHomeCryptoService()
        val vm = HomeAccountViewModel(ClearAccountUseCase(cryptoService), dispatcher)

        vm.clearAccountInStorage()
        advanceUntilIdle()

        assertTrue(cryptoService.cleared)
    }
}
