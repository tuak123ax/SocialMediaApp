package com.minhtu.firesocialmedia.notification.presentation.loading

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoadingViewModelTest {

    @Test
    fun `isLoading defaults to false`() {
        val vm = LoadingViewModel()

        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `showLoading sets isLoading to true`() {
        val vm = LoadingViewModel()

        vm.showLoading()

        assertTrue(vm.isLoading.value)
    }

    @Test
    fun `hideLoading sets isLoading to false`() {
        val vm = LoadingViewModel()

        vm.showLoading()
        vm.hideLoading()

        assertFalse(vm.isLoading.value)
    }
}
