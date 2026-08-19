package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance

class StoreNewsToRoomUseCase(
    private val homeNewsRoomService: HomeNewsRoomService
) {
    suspend operator fun invoke(news: List<NewsInstance>) {
        homeNewsRoomService.storeNewsToRoom(news)
    }
}
