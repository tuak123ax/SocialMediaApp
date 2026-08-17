package com.minhtu.firesocialmedia.presentation.settings

import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.security.LocalDataCleaner
import com.minhtu.firesocialmedia.testutil.FakeSecurityCryptoService
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clearAccountInStorage invokes crypto service clearAccount`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val crypto = FakeSecurityCryptoService()
        val vm = AccountViewModel(
            ClearAccountUseCase(crypto),
            ClearLocalDataUseCase(emptyList()),
            dispatcher
        )

        vm.clearAccountInStorage()
        advanceUntilIdle()

        assertEquals(1, crypto.clearAccountInvokedCount)
    }

    @Test
    fun `clearLocalData invokes all registered cleaners`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val crypto = FakeSecurityCryptoService()
        var cleaner1Called = false
        var cleaner2Called = false
        val vm = AccountViewModel(
            ClearAccountUseCase(crypto),
            ClearLocalDataUseCase(listOf(
                LocalDataCleaner { cleaner1Called = true },
                LocalDataCleaner { cleaner2Called = true }
            )),
            dispatcher
        )

        vm.clearLocalData()
        advanceUntilIdle()

        assertTrue(cleaner1Called)
        assertTrue(cleaner2Called)
    }
}
