package com.minhtu.firesocialmedia.presentation.showimage

import com.minhtu.firesocialmedia.domain.usecases.showimage.DownloadImageUseCase
import com.minhtu.firesocialmedia.testutil.FakeShowImageRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ShowImageViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `downloadImage success invokes use case with the given image and file name`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeShowImageRepository().apply { downloadResult = true }
        val vm = ShowImageViewModel(DownloadImageUseCase(repo), dispatcher)

        vm.downloadImage("https://example.com/img.png", "img.png")
        advanceUntilIdle()

        assertEquals("https://example.com/img.png" to "img.png", repo.lastArgs)
    }

    @Test
    fun `downloadImage failure still invokes the use case without throwing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeShowImageRepository().apply { downloadResult = false }
        val vm = ShowImageViewModel(DownloadImageUseCase(repo), dispatcher)

        vm.downloadImage("https://example.com/broken.png", "broken.png")
        advanceUntilIdle()

        assertEquals("https://example.com/broken.png" to "broken.png", repo.lastArgs)
    }
}
