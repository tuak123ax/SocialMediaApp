package com.minhtu.firesocialmedia.auth.presentation.loading

import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadingViewModelTest {

    @Test
    fun `isLoading starts false`() {
        val viewModel = LoadingViewModel()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `showLoading sets isLoading to true`() {
        val viewModel = LoadingViewModel()
        viewModel.showLoading()
        assertEquals(true, viewModel.isLoading.value)
    }

    @Test
    fun `hideLoading sets isLoading to false`() {
        val viewModel = LoadingViewModel()
        viewModel.showLoading()
        viewModel.hideLoading()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `hideLoading is a no-op when already hidden`() {
        val viewModel = LoadingViewModel()
        viewModel.hideLoading()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `showLoading is idempotent`() {
        val viewModel = LoadingViewModel()
        viewModel.showLoading()
        viewModel.showLoading()
        assertEquals(true, viewModel.isLoading.value)
    }
}
