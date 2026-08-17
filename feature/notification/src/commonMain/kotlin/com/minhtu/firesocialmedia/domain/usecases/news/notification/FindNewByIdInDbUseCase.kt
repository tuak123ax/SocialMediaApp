package com.minhtu.firesocialmedia.domain.usecases.news.notification

import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.repository.news.NotificationNewsRepository

class FindNewByIdInDbUseCase(
    private val notificationNewsRepository : NotificationNewsRepository
) {
    suspend operator fun invoke(newId : String) : NewsInstance {
        return notificationNewsRepository.getNew(newId) ?: NewsInstance()
    }
}
