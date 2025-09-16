package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class SaveFriendUseCase(
    private val commonDbRepository: CommonDbRepository
) {
    suspend operator fun invoke(id : String,
                                value : ArrayList<String>) {
        commonDbRepository.saveFriend(id ,value)
    }
}