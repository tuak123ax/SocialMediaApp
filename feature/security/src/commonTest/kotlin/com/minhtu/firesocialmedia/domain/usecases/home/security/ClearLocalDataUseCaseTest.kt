package com.minhtu.firesocialmedia.domain.usecases.home.security

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearLocalDataUseCaseTest {

    @Test
    fun `invoke calls clear on every registered cleaner`() = runTest {
        val calledOrder = mutableListOf<Int>()
        val cleaners = listOf(
            LocalDataCleaner { calledOrder.add(1) },
            LocalDataCleaner { calledOrder.add(2) },
            LocalDataCleaner { calledOrder.add(3) }
        )
        val useCase = ClearLocalDataUseCase(cleaners)

        useCase()

        assertEquals(listOf(1, 2, 3), calledOrder)
    }

    @Test
    fun `invoke with no cleaners completes without error`() = runTest {
        val useCase = ClearLocalDataUseCase(emptyList())
        useCase()
        assertTrue(true)
    }
}
