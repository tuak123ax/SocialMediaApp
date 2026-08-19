package com.minhtu.firesocialmedia.profile.presentation.loading

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoadingViewModelTest {

    @Test
    fun `isLoading starts as false`() {
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

    @Test
    fun `showLoading then hideLoading then showLoading again`() {
        val vm = LoadingViewModel()
        vm.showLoading()
        assertEquals(true, vm.isLoading.value)
        vm.hideLoading()
        assertEquals(false, vm.isLoading.value)
        vm.showLoading()
        assertEquals(true, vm.isLoading.value)
    }
}
