package com.minhtu.firesocialmedia.group.presentation.loading

import kotlin.test.Test
import kotlin.test.assertEquals

class LoadingViewModelTest {

    @Test
    fun `isLoading starts as false`() {
        val vm = LoadingViewModel()
        assertEquals(false, vm.isLoading.value)
    }

    @Test
    fun `showLoading sets isLoading to true`() {
        val vm = LoadingViewModel()
        vm.showLoading()
        assertEquals(true, vm.isLoading.value)
    }

    @Test
    fun `hideLoading sets isLoading to false`() {
        val vm = LoadingViewModel()
        vm.showLoading()
        vm.hideLoading()
        assertEquals(false, vm.isLoading.value)
    }

    @Test
    fun `multiple showLoading calls keep isLoading true`() {
        val vm = LoadingViewModel()
        vm.showLoading()
        vm.showLoading()
        assertEquals(true, vm.isLoading.value)
    }
}
