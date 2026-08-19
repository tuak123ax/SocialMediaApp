package com.minhtu.firesocialmedia.presentation.loading

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncLoadingViewModelTest {

    @Test
    fun `initial isSyncing state is false`() {
        val vm = SyncLoadingViewModel()

        assertFalse(vm.isSyncing.value)
    }

    @Test
    fun `showSyncLoading sets isSyncing to true`() {
        val vm = SyncLoadingViewModel()

        vm.showSyncLoading()

        assertTrue(vm.isSyncing.value)
    }

    @Test
    fun `hideSyncLoading sets isSyncing to false`() {
        val vm = SyncLoadingViewModel()

        vm.showSyncLoading()
        vm.hideSyncLoading()

        assertFalse(vm.isSyncing.value)
    }

    @Test
    fun `hideSyncLoading is idempotent when already hidden`() {
        val vm = SyncLoadingViewModel()

        vm.hideSyncLoading()

        assertFalse(vm.isSyncing.value)
    }
}
