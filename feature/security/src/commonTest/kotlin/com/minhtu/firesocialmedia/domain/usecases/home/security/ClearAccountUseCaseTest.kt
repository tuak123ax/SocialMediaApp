package com.minhtu.firesocialmedia.domain.usecases.home.security

import com.minhtu.firesocialmedia.testutil.FakeSecurityCryptoService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ClearAccountUseCaseTest {

    @Test
    fun `invoke clears the account through the crypto service`() = runTest {
        val crypto = FakeSecurityCryptoService()
        val useCase = ClearAccountUseCase(crypto)

        useCase()

        assertEquals(1, crypto.clearAccountInvokedCount)
    }
}
