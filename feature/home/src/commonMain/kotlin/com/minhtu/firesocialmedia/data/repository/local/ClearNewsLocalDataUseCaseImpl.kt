package com.minhtu.firesocialmedia.data.repository.local

import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService

class ClearNewsLocalDataUseCaseImpl(
    private val newsRoomService: HomeNewsRoomService
) {
    suspend operator fun invoke() {
        newsRoomService.clearLikedPosts()
    }
}
