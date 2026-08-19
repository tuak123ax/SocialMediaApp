package com.minhtu.firesocialmedia.presentation.setting.notificationconfigs

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationConfigsViewModelTest {

    @Test
    fun `default settings enable all notification types`() {
        val vm = NotificationConfigsViewModel()

        val settings = vm.notificationSettings.value
        assertEquals(true, settings[NotificationType.LIKE])
        assertEquals(true, settings[NotificationType.COMMENT])
        assertEquals(true, settings[NotificationType.ADD_FRIEND])
        assertEquals(true, settings[NotificationType.UPLOAD_NEW])
        assertEquals(true, settings[NotificationType.SHARE_NEW])
        assertEquals(true, settings[NotificationType.INVITE_TO_GROUP])
    }

    @Test
    fun `updateNotification toggles a specific notification type`() {
        val vm = NotificationConfigsViewModel()

        vm.updateNotification(NotificationType.LIKE, false)
        assertEquals(false, vm.notificationSettings.value[NotificationType.LIKE])

        vm.updateNotification(NotificationType.LIKE, true)
        assertEquals(true, vm.notificationSettings.value[NotificationType.LIKE])
    }

    @Test
    fun `updateNotification does not affect other notification types`() {
        val vm = NotificationConfigsViewModel()

        vm.updateNotification(NotificationType.COMMENT, false)

        assertEquals(false, vm.notificationSettings.value[NotificationType.COMMENT])
        assertEquals(true, vm.notificationSettings.value[NotificationType.LIKE])
        assertEquals(true, vm.notificationSettings.value[NotificationType.ADD_FRIEND])
    }
}
