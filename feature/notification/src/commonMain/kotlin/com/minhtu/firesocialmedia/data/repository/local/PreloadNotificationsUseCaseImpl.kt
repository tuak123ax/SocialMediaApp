package com.minhtu.firesocialmedia.data.repository.local

import com.minhtu.firesocialmedia.presentation.notification.NotificationsViewModel

class PreloadNotificationsUseCaseImpl(
    private val notificationsViewModel: NotificationsViewModel
) {
    operator fun invoke() {
        notificationsViewModel.getAllNotificationsOfUser()
    }
}
