package com.minhtu.firesocialmedia.home.presentation.loading

import kotlin.test.Test
import kotlin.test.assertEquals

class LoadingViewModelTest {

    @Test
    fun `initial loading state is false`() {
        val vm = LoadingViewModel()
        assertEquals(false, vm.isLoading.value)
    }

    @Test
    fun `showLoading sets isLoading true`() {
        val vm = LoadingViewModel()
        vm.showLoading()
        assertEquals(true, vm.isLoading.value)
    }

    @Test
    fun `hideLoading sets isLoading false`() {
        val vm = LoadingViewModel()
        vm.showLoading()
        vm.hideLoading()
        assertEquals(false, vm.isLoading.value)
    }
}
