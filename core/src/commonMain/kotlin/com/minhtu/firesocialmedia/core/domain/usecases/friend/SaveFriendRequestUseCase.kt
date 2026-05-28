package com.minhtu.firesocialmedia.core.domain.usecases.friend

import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository

class SaveFriendRequestUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : ArrayList<String>) {
        commonDbRepository.saveFriendRequest(id, value)
    }
}