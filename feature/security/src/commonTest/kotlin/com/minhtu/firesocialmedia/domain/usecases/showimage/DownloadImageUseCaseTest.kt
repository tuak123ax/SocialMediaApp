package com.minhtu.firesocialmedia.domain.usecases.showimage

import com.minhtu.firesocialmedia.testutil.FakeShowImageRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadImageUseCaseTest {

    @Test
    fun `invoke returns true and forwards args on success`() = runTest {
        val repo = FakeShowImageRepository().apply { downloadResult = true }
        val useCase = DownloadImageUseCase(repo)

        val result = useCase("https://example.com/img.png", "img.png")

        assertTrue(result)
        assertEquals("https://example.com/img.png" to "img.png", repo.lastArgs)
    }

    @Test
    fun `invoke returns false when the repository fails`() = runTest {
        val repo = FakeShowImageRepository().apply { downloadResult = false }
        val useCase = DownloadImageUseCase(repo)

        val result = useCase("https://example.com/broken.png", "broken.png")

        assertFalse(result)
    }
}
