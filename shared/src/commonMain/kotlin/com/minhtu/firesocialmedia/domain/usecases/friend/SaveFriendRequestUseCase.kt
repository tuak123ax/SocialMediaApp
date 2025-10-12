package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveFriendRequestUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : ArrayList<String>) {
        commonDbRepository.saveFriendRequest(id, value)
    }
}